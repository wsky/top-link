﻿using System;
using System.Collections.Generic;
using System.Text;
using Taobao.Top.Link.Channel;

namespace Taobao.Top.Link.Endpoints
{
    // Abstract network model
    // https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit
    public sealed class Endpoint
    {
        internal static int TIMOUT = 5000;
        private ILog _log;
        private IClientChannelSelector _channelSelector;
        // in/out endpoints
        private IList<EndpointProxy> _connected;
        private EndpointHandler _handler;

        /// <summary>message received, see RTT based
        /// </summary>
        public event EventHandler<EndpointContext> OnMessage;
        /// <summary>ack message received, see RTT based
        /// </summary>
        public event EventHandler<AckMessageArgs> OnAckMessage;

        /// <summary>get id
        /// </summary>
        public Identity Identity { get; private set; }

        public Endpoint(Identity identity) : this(DefaultLoggerFactory.Default, identity) { }
        public Endpoint(ILoggerFactory loggerFactory, Identity identity)
        {
            this._connected = new List<EndpointProxy>();
            this._log = loggerFactory.Create(this);
            this.Identity = identity;
            this._channelSelector = new ClientChannelSharedSelector(loggerFactory);
            this._handler = new EndpointHandler(loggerFactory);
            this._handler.MessageHandler = ctx => OnMessage(this, ctx);
            this._handler.AckMessageHandler = (m, i) => OnAckMessage(this, new AckMessageArgs(m, i));
        }

        /// <summary>get connected endpoint by id
        /// </summary>
        /// <param name="target"></param>
        /// <returns></returns>
        public EndpointProxy GetEndpoint(Identity target)
        {
            if (target.Equals(this.Identity))
                throw new LinkException(Text.E_ID_DUPLICATE);
            foreach (EndpointProxy p in this._connected)
                if (p.Identity != null && p.Identity.Equals(target))
                    return p;
            return null;
        }
        /// <summary>connect endpoint
        /// </summary>
        /// <param name="target">target id</param>
        /// <param name="uri">target address</param>
        /// <returns></returns>
        public EndpointProxy GetEndpoint(Identity target, string uri)
        {
            return this.GetEndpoint(target, uri, null);
        }
        /// <summary>connect endpoint
        /// </summary>
        /// <param name="target">target id</param>
        /// <param name="uri">target address</param>
        /// <param name="extras">passed as connect message</param>
        /// <returns></returns>
        public EndpointProxy GetEndpoint(Identity target, string uri, IDictionary<string, string> extras)
        {
            EndpointProxy e = this.GetEndpoint(target) ?? this.CreateProxy();
            e.Identity = target;
            // always clear, cached proxy will have broken channel
            e.Remove(uri);
            // always reget channel, make sure it's valid
            IClientChannel channel = this._channelSelector.GetChannel(new Uri(uri));
            e.Add(channel);
            // connect message
            Message msg = new Message();
            msg.MessageType = MessageType.CONNECT;
            IDictionary<string, string> content = new Dictionary<string, string>();
            this.Identity.Render(content);
            // pass extra data
            if (extras != null)
                foreach (var p in extras)
                    content.Add(p);
            msg.Content = content;
            this._handler.SendAndWait(e, channel, msg, TIMOUT);
            return e;
        }

        private EndpointProxy CreateProxy()
        {
            EndpointProxy e = new EndpointProxy(this._handler);
            this._connected.Add(e);
            return e;
        }
    }
}