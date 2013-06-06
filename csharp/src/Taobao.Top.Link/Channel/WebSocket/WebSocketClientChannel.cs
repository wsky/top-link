using System;
using System.Collections.Generic;
using System.Text;
using WebSocketSharp;
using WebSocketSharp.Frame;

namespace Taobao.Top.Link.Channel.WebSocket
{
    /// <summary>websocket clientchannel via websocket-sharp impl
    /// </summary>
    public class WebSocketClientChannel : IClientChannel
    {
        private WebSocketSharp.WebSocket _socket;

        public EventHandler<ChannelContext> OnMessage { get; set; }
        public EventHandler<ChannelContext> OnError { get; set; }
        public EventHandler<ChannelClosedEventArgs> OnClosed { get; set; }

        public Uri Uri { get; set; }
        public bool IsConnected { get { return this._socket.ReadyState == WsState.OPEN; } }

        public WebSocketClientChannel(WebSocketSharp.WebSocket socket)
        {
            this._socket = socket;
        }

        public void Send(byte[] data)
        {
            this._socket.Send(data);
        }

        public void Close(string reason)
        {
            this._socket.Close(CloseStatusCode.NORMAL, reason);
        }
    }
}