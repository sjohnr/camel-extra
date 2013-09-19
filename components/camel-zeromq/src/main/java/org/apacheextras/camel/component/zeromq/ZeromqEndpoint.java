/**************************************************************************************
 http://code.google.com/a/apache-extras.org/p/camel-extra

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.


 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301, USA.

 http://www.gnu.org/licenses/gpl-2.0-standalone.html
 ***************************************************************************************/
package org.apacheextras.camel.component.zeromq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

public class ZeromqEndpoint extends DefaultEndpoint {

    private static final String URI_ERROR = "Invalid URI. Format must be of the form zeromq:(tcp|icp)://hostname:port[?options...]";

    private final String protocol;
    private final String hostname;
    private boolean messageIdEnabled;
    private final int port;
    private ZeromqSocketType socketType;
    private long highWaterMark = -1;
    private long linger = -1;
    private String topics;
    private boolean asyncConsumer = true;
    private Class messageConvertor = DefaultMessageConvertor.class;
    private SocketFactory socketFactory;
    private ContextFactory contextFactory;

    public ZeromqEndpoint(String endpointUri, String remaining, ZeromqComponent component) throws URISyntaxException {
        super(endpointUri, component);

        URI uri = new URI(remaining);

        protocol = uri.getScheme();
        if (protocol == null)
            throw new ZeromqException(URI_ERROR);

        if (!protocol.equalsIgnoreCase("TCP") && !protocol.equalsIgnoreCase("IPC"))
            throw new ZeromqException(URI_ERROR);

        hostname = uri.getHost();
        if (hostname == null)
            throw new ZeromqException(URI_ERROR);

        port = uri.getPort();
        if (port < 0)
            throw new ZeromqException(URI_ERROR);

        this.socketFactory = new AkkaSocketFactory(highWaterMark, linger);
        this.contextFactory = new AkkaContextFactory();
    }

    @Override
    public ZeromqConsumer createConsumer(Processor processor) throws Exception {
        if (socketType == null)
            throw new ZeromqException("Must specify socket type as a parameter, eg socketType=SUBSCRIBE");
        return new ZeromqConsumer(this, processor, contextFactory, socketFactory);
    }

    @Override
    public ZeromqProducer createProducer() throws Exception {
        if (socketType == null)
            throw new ZeromqException("Must specify socket type as a parameter, eg socketType=PUBLISH");
        return new ZeromqProducer(this, socketFactory, contextFactory);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    Exchange createZeromqExchange(byte[] body) {
        Exchange exchange = new DefaultExchange(getCamelContext(), getExchangePattern());

        Message message = new DefaultMessage();
        message.setHeader(ZeromqConstants.HEADER_SOURCE, getSocketAddress());
        message.setHeader(ZeromqConstants.HEADER_SOCKET_TYPE, socketType);
        message.setHeader(ZeromqConstants.HEADER_TIMESTAMP, System.currentTimeMillis());
        if (isMessageIdEnabled())
            message.setHeader(ZeromqConstants.HEADER_MSG_ID, UUID.randomUUID().toString());

        message.setBody(body);
        exchange.setIn(message);

        return exchange;
    }

    public long getHighWaterMark() {
        return highWaterMark;
    }

    public String getHostname() {
        return hostname;
    }

    public long getLinger() {
        return linger;
    }

    public Class getMessageConvertor() {
        return messageConvertor;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    String getSocketAddress() {
        String addr = getProtocol() + "://" + getHostname() + ":" + getPort();
        return addr;
    }

    public ZeromqSocketType getSocketType() {
        return socketType;
    }

    public String getTopics() {
        return topics;
    }

    public boolean isAsyncConsumer() {
        return asyncConsumer;
    }

    public boolean isMessageIdEnabled() {
        return messageIdEnabled;
    }

    public void setAsyncConsumer(boolean asyncConsumer) {
        this.asyncConsumer = asyncConsumer;
    }

    public void setContextFactory(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public void setHighWaterMark(long highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public void setLinger(long linger) {
        this.linger = linger;
    }

    public void setMessageConvertor(Class messageConvertor) {
        this.messageConvertor = messageConvertor;
    }

    public void setMessageIdEnabled(boolean messageIdEnabled) {
        this.messageIdEnabled = messageIdEnabled;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public void setSocketType(ZeromqSocketType socketType) {
        this.socketType = socketType;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

}