using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Endpoints
{
    /// <summary>for endpoints talking
    /// </summary>
    public class Message
    {
        public short MessageType { get; set; }
        public int StatusCode { get; set; }
        public string StatusPhase { get; set; }
        public int Flag { get; set; }
        public string Token { get; set; }
        public IDictionary<string, string> Content { get; set; }
    }
}