using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Channel
{
    /// <summary>context used with channel event
    /// </summary>
    public class ChannelContext : EventArgs
    {
        /// <summary>error from channel
        /// </summary>
        public Exception Error { get; set; }
        /// <summary>the channel used to sending message
        /// </summary>
        public IChannelSender Sender { get; set; }
        /// <summary>received message
        /// </summary>
        public object Message { get; set; }
        /// <summary>
        /// send data to channel where the message come from
        /// </summary>
        /// <param name="data"></param>
        public void Reply(byte[] data)
        {
            this.Sender.Send(data);
        }
    }
}