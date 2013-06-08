using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace Taobao.Top.Link.Remoting
{
    /// <summary>support wait until got methodReturn
    /// </summary>
    public class SynchronizedRemotingCallback : RemotingCallback
    {
        private EventWaitHandle _handle;

        /// <summary>get unexpected error that from network or framework
        /// </summary>
        public Exception Exception { get; private set; }
        /// <summary>get remote method result
        /// </summary>
        public MethodReturn MethodReturn { get; private set; }

        public SynchronizedRemotingCallback(string serializationFormat
            , Type returnType)
            : base(serializationFormat
            , returnType)
        {
            this._handle = new EventWaitHandle(false, EventResetMode.AutoReset);
        }

        public override void OnException(Exception exception)
        {
            this.Exception = exception;
            this._handle.Set();
        }
        public override void OnMethodReturn(MethodReturn methodReturn)
        {
            this.MethodReturn = methodReturn;
            this._handle.Set();
        }

        /// <summary>wait until got methodReturn 
        /// </summary>
        /// <param name="timeout">timeout in milliseconds</param>
        public void Wait(int timeout)
        {
            if (timeout > 0)
            {
                if (!this._handle.WaitOne(timeout))
                    throw new RemotingException("remoting execution timeout");
            }
            else
                this._handle.WaitOne();
        }
    }
}