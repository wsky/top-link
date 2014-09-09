using System;
using System.Collections.Generic;
using System.Text;

namespace Top.Link.Remoting
{
    /// <summary>rpc call object
    /// </summary>
    public class MethodCall
    {
        /// <summary>request object uri
        /// </summary>
        public string Uri { get; set; }
        /// <summary>remote method
        /// </summary>
        public string MethodName { get; set; }
        /// <summary>remote type
        /// </summary>
        public string TypeName { get; set; }
        /// <summary>for override method
        /// </summary>
        public Type[] MethodSignature { get; set; }
        /// <summary>method arguments
        /// </summary>
        public object[] Args { get; set; }
    }
}