using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using WebSocketSharp;

namespace Taobao.Top.Link.Channel.WebSocket
{
    public static class WebSocketClient
    {
        /// <summary>connect to uri via websocket
        /// </summary>
        /// <param name="uri">remote address</param>
        /// <param name="timeout">timeout in milliseconds</param>
        /// <returns></returns>
        public static IClientChannel Connect(Uri uri, int timeout)
        {
            var h = new WaitHandle();
            var onOpen = new EventHandler((o, e) => h.Set());
            var onError = new EventHandler<ErrorEventArgs>((o, e) => h.Set(e.Message));

            var socket = new WebSocketSharp.WebSocket(uri.ToString());
            var channel = new WebSocketClientChannel(socket);
            socket.OnOpen += onOpen;
            socket.OnError += onError;

            socket.Connect();

            if (!h.WaitOne(timeout))
                throw new LinkException("connect timeout");
            if (h.IsError)
                throw new LinkException(h.Error);

            socket.OnOpen -= onOpen;
            socket.OnError -= onError;

            socket.OnError += (o, e) => Raise(channel.OnClosed, new ChannelContext());
            socket.OnClose += (o, e) => Raise(channel.OnClosed, new ChannelContext());
            socket.OnMessage += (o, e) => Raise(channel.OnClosed, new ChannelContext());
            return channel;
        }

        static void Raise(EventHandler<ChannelContext> eventHandler, ChannelContext context)
        {
            if (eventHandler != null)
                eventHandler(null, context);
        }

        class WaitHandle : EventWaitHandle
        {
            public bool IsError { get; private set; }
            public string Error { get; private set; }
            public WaitHandle() : base(false, EventResetMode.AutoReset) { }
            public void Set(string error)
            {
                this.IsError = true;
                this.Error = error;
                this.Set();
            }
        }
    }
}