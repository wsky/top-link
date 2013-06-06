using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Remoting
{
    /// <summary>called when method return got
    /// </summary>
    public abstract class RemotingCallback
    {
        /// <summary>get call flag
        /// </summary>
        public int Flag { get; internal set; }
        /// <summary>get call/return serialization
        /// </summary>
        public string SerializationFormat { get; private set; }
        /// <summary>get return object type
        /// </summary>
        public Type ReturnType { get; private set; }

        public abstract void OnException(Exception exception);
        public abstract void OnMethodReturn(MethodReturn methodReturn);

        public RemotingCallback(string serializationFormat, Type returnType)
        {
            this.SerializationFormat = serializationFormat;
            this.ReturnType = returnType;
        }
    }
}