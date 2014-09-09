using System;
using System.Collections.Generic;
using System.Text;

namespace Top.Link.Remoting
{
    /// <summary>rpc call result
    /// </summary>
    public class MethodReturn
    {
        /// <summary>call method result
        /// </summary>
        public object ReturnValue { get; set; }
        /// <summary>error of call if occur
        /// </summary>
        public Exception Exception { get; set; }
    }
}