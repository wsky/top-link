using System;
using System.Collections.Generic;
using System.Text;
using Taobao.Top.Link.Channel.WebSocket;

namespace Taobao.Top.Link.Channel
{
    /// <summary>a channel pool that same uri sharing same channel
    /// </summary>
    public class ClientChannelSharedSelector : IClientChannelSelector
    {
        private Object _lockObject;
        private IDictionary<string, IClientChannel> _channels;

        public ClientChannelSharedSelector()
        {
            this._lockObject = new object();
            this._channels = new Dictionary<string, IClientChannel>();
        }

        public IClientChannel GetChannel(Uri uri)
        {
            if (!uri.Scheme.Equals("ws", StringComparison.InvariantCultureIgnoreCase))
                return null;

            var url = uri.ToString();

            if (!this.HaveChannel(url))
                lock (this._lockObject)
                    if (!this.HaveChannel(url))
                        this._channels.Add(url, this.WrapChannel(this.Connect(uri, 5000)));

            return _channels[url];
        }
        public void ReturnChannel(IClientChannel channel) { }

        protected virtual IClientChannel Connect(Uri uri, int timeout)
        {
            return WebSocketClient.Connect(uri, timeout);
        }
        private IClientChannel WrapChannel(IClientChannel channel)
        {
            return channel;
        }
        private bool HaveChannel(string url)
        {
            return this._channels.ContainsKey(url) && this._channels[url].IsConnected;
        }
    }
}