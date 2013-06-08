using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Endpoints
{
    public class AckMessageArgs : EventArgs
    {
        /// <summary>get ack message
        /// </summary>
        public IDictionary<string, string> Message { get; private set; }
        /// <summary>get where the ack message come from
        /// </summary>
        public Identity MessageFrom { get; private set; }

        public AckMessageArgs(IDictionary<string, string> message, Identity messageFrom)
        {
            this.Message = message;
            this.MessageFrom = messageFrom;
        }
    }
}