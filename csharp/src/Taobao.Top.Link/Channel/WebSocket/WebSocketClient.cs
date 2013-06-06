using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using WebSocketSharp;

namespace Taobao.Top.Link.Channel.WebSocket
{
    /// <summary>simple websocket client helper
    /// </summary>
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

            socket.OnError += (o, e) => On(channel.OnError
                , new ChannelContext(new LinkException(e.Message)));
            socket.OnClose += (o, e) => On(channel.OnClosed
                , new ChannelClosedEventArgs(e.Reason));
            socket.OnMessage += (o, e) => On(channel.OnMessage
                , new ChannelContext(e.RawData, channel));
            return channel;
        }

        private static void On<T>(EventHandler<T> eventHandler, T args) where T : EventArgs
        {
            try
            {
                if (eventHandler != null)
                    eventHandler(null, args);
            }
            catch (Exception e)
            {
                //here is global on error
                Console.WriteLine(e.Message);
                //TODO:close channel here?
            }
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