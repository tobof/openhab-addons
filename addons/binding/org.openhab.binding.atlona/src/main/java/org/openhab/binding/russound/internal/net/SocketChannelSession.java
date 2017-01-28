package org.openhab.binding.russound.internal.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a restartable socket connection to the underlying telnet session. Commands can be sent via
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}. This implementation
 * of {@link SocketSession} communicates using a {@link SocketChannel} connection.
 *
 * @author Tim Roberts
 */
public class SocketChannelSession implements SocketSession {
    private Logger _logger = LoggerFactory.getLogger(SocketChannelSession.class);

    /**
     * The host/ip address to connect to
     */
    private final String _host;

    /**
     * The port to connect to
     */
    private final int _port;

    /**
     * The actual socket being used. Will be null if not connected
     */
    private final AtomicReference<SocketChannel> _socketChannel = new AtomicReference<SocketChannel>();

    /**
     * The {@link ResponseReader} that will be used to read from {@link #_readBuffer}
     */
    private final ResponseReader _responseReader = new ResponseReader();

    /**
     * The responses read from the {@link #_responseReader}
     */
    private final BlockingQueue<Object> _responses = new ArrayBlockingQueue<Object>(50);

    /**
     * The dispatcher of responses from {@link #_responses}
     */
    private final Dispatcher _dispatcher = new Dispatcher();

    /**
     * The {@link SocketSessionListener} that the {@link #_dispatcher} will call
     */
    private List<SocketSessionListener> _listeners = new CopyOnWriteArrayList<SocketSessionListener>();

    /**
     * Creates the socket session from the given host and port
     *
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public SocketChannelSession(String host, int port) {
        if (host == null || host.trim().length() == 0) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        _host = host;
        _port = port;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.russound.internal.net.SocketSession#addListener(org.openhab.binding.russound.internal.net.
     * SocketSessionListener)
     */
    @Override
    public void addListener(SocketSessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        _listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#clearListeners()
     */
    @Override
    public void clearListeners() {
        _listeners.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.russound.internal.net.SocketSession#removeListener(org.openhab.binding.russound.internal.net.
     * SocketSessionListener)
     */
    @Override
    public boolean removeListener(SocketSessionListener listener) {
        return _listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#connect()
     */
    @Override
    public void connect() throws IOException {
        disconnect();

        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(true);

        _logger.debug("Connecting to {}:{}", _host, _port);
        channel.connect(new InetSocketAddress(_host, _port));

        _logger.debug("Waiting for connect");
        while (!channel.finishConnect()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }

        _socketChannel.set(channel);
        new Thread(_dispatcher).start();
        new Thread(_responseReader).start();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#disconnect()
     */
    @Override
    public void disconnect() throws IOException {
        if (isConnected()) {
            _logger.debug("Disconnecting from {}:{}", _host, _port);

            final SocketChannel channel = _socketChannel.getAndSet(null);
            channel.close();

            _dispatcher.stopRunning();
            _responseReader.stopRunning();

            _responses.clear();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#isConnected()
     */
    @Override
    public boolean isConnected() {
        final SocketChannel channel = _socketChannel.get();
        return channel != null && channel.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#sendCommand(java.lang.String)
     */
    @Override
    public synchronized void sendCommand(String command) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }

        // if (command.trim().length() == 0) {
        // throw new IllegalArgumentException("Command cannot be empty");
        // }

        if (!isConnected()) {
            throw new IOException("Cannot send message - disconnected");
        }

        ByteBuffer toSend = ByteBuffer.wrap((command + "\r\n").getBytes());

        final SocketChannel channel = _socketChannel.get();
        if (channel == null) {
            _logger.debug("Cannot send command '{}' - socket channel was closed", command);
        } else {
            _logger.debug("Sending Command: '{}'", command);
            channel.write(toSend);
        }
    }

    /**
     * This is the runnable that will read from the socket and add messages to the responses queue (to be processed by
     * the dispatcher)
     *
     * @author Tim Roberts
     *
     */
    private class ResponseReader implements Runnable {

        /**
         * Whether the reader is currently running
         */
        private final AtomicBoolean _isRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final CountDownLatch _running = new CountDownLatch(1);

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop (should stop within 1 second based on the
         * setSOTimeout)
         */
        public void stopRunning() {
            if (_isRunning.getAndSet(false)) {
                try {
                    if (!_running.await(5, TimeUnit.SECONDS)) {
                        _logger.warn("Waited too long for response reader to finish");
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }

        /**
         * Runs the logic to read from the socket until {@link #_isRunning} is false. A 'response' is anything that ends
         * with a carriage-return/newline combo. Additionally, the special "Login: " and "Password: " prompts are
         * treated as responses for purposes of logging in.
         */
        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder(100);
            final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            _isRunning.set(true);
            _responses.clear();

            while (_isRunning.get()) {
                try {
                    // if reader is null, sleep and try again
                    if (readBuffer == null) {
                        Thread.sleep(250);
                        continue;
                    }

                    final SocketChannel channel = _socketChannel.get();
                    if (channel == null) {
                        // socket was closed
                        _isRunning.set(false);
                        break;
                    }

                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead == -1) {
                        _responses.put(new IOException("server closed connection"));
                        _isRunning.set(false);
                        break;
                    } else if (bytesRead == 0) {
                        readBuffer.clear();
                        continue;
                    }

                    readBuffer.flip();
                    while (readBuffer.hasRemaining()) {
                        final char ch = (char) readBuffer.get();
                        sb.append(ch);
                        if (ch == '\n' || ch == ' ') {
                            final String str = sb.toString();
                            if (str.endsWith("\r\n") || str.endsWith("Login: ") || str.endsWith("Password: ")) {
                                sb.setLength(0);
                                final String response = str.substring(0, str.length() - 2);
                                _responses.put(response);
                            }
                        }
                    }

                    readBuffer.flip();

                } catch (InterruptedException e) {
                    // Do nothing - probably shutting down
                } catch (AsynchronousCloseException e) {
                    // socket was definitelyclosed by another thread
                } catch (IOException e) {
                    try {
                        _isRunning.set(false);
                        _responses.put(e);
                    } catch (InterruptedException e1) {
                        // Do nothing - probably shutting down
                    }
                }
            }

            _running.countDown();
        }
    }

    /**
     * The dispatcher runnable is responsible for reading the response queue and dispatching it to the current callable.
     * Since the dispatcher is ONLY started when a callable is set, responses may pile up in the queue and be dispatched
     * when a callable is set. Unlike the socket reader, this can be assigned to another thread (no state outside of the
     * class).
     *
     * @author Tim Roberts
     */
    private class Dispatcher implements Runnable {

        /**
         * Whether the dispatcher is running or not
         */
        private final AtomicBoolean _isRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final CountDownLatch _running = new CountDownLatch(1);

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop (should stop within 1 second based on the poll
         * timeout below)
         */
        public void stopRunning() {

            if (_isRunning.getAndSet(false)) {
                try {
                    if (!_running.await(5, TimeUnit.SECONDS)) {
                        _logger.warn("Waited too long for dispatcher to finish");
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        /**
         * Runs the logic to dispatch any responses to the current listeners until {@link #_isRunning} is false.
         */
        @Override
        public void run() {
            _isRunning.set(true);
            while (_isRunning.get()) {
                try {
                    final SocketSessionListener[] listeners = _listeners.toArray(new SocketSessionListener[0]);

                    // if no listeners, we don't want to start dispatching yet.
                    if (listeners.length == 0) {
                        Thread.sleep(250);
                        continue;
                    }

                    final Object response = _responses.poll(1, TimeUnit.SECONDS);

                    if (response != null) {
                        if (response instanceof String) {
                            try {
                                _logger.debug("Dispatching response: {}", response);
                                for (SocketSessionListener listener : listeners) {
                                    listener.responseReceived((String) response);
                                }
                            } catch (Exception e) {
                                _logger.warn("Exception occurred processing the response '{}': {}", response, e);
                            }
                        } else if (response instanceof Exception) {
                            _logger.debug("Dispatching exception: {}", response);
                            for (SocketSessionListener listener : listeners) {
                                listener.responseException((Exception) response);
                            }
                        } else {
                            _logger.warn("Unknown response class: {}", response);
                        }
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
            _isRunning.set(false);

            _running.countDown();
        }
    }
}
