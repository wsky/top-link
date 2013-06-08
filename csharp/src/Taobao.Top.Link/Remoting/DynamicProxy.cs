using System;
using System.Collections.Generic;
using System.Reflection;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Remoting.Proxies;
using System.Text;
using RemotingProtocolParser.TCP;
using Taobao.Top.Link.Channel;

namespace Taobao.Top.Link.Remoting
{
    /// <summary>proxy any type that for top-link.remoting rpc call, proxy based on .net build-in realproxy
    /// </summary>
    public class DynamicProxy : RealProxy
    {
        //HACK:base on .net remoting implementation
        private static readonly string RETURN = "__Return";

        private Uri _remoteUri;
        private string _remoteUriString;
        private RemotingHandler _handler;
        public int ExecutionTimeout { get; set; }
        public string SerializationFormat { get; set; }

        public DynamicProxy(Type classToProxy
            , Uri remoteUri
            , RemotingHandler handler)
            : base(classToProxy)
        {
            this._remoteUri = remoteUri;
            this._remoteUriString = this._remoteUri.ToString();
            this._handler = handler;
        }
        /// <summary>
        /// just call by realproxy
        /// </summary>
        /// <param name="msg"></param>
        /// <returns></returns>
        public override IMessage Invoke(IMessage msg)
        {
            var call = msg as IMethodCallMessage;

            MethodReturn r = this.Invoke(new MethodCall()
            {
                Uri = this._remoteUriString,
                MethodName = call.MethodName,
                TypeName = call.TypeName,
                MethodSignature = (Type[])call.MethodSignature,
                Args = call.Args
            }, (call.MethodBase as MethodInfo).ReturnType);

            if (r.Exception != null)
                throw r.Exception;

            return new MethodResponse(new Header[] { new Header(RETURN, r.ReturnValue) }, call);
        }
        /// <summary>invoke rpc call
        /// </summary>
        /// <param name="call"></param>
        /// <param name="returnType"></param>
        /// <returns></returns>
        public MethodReturn Invoke(MethodCall call, Type returnType)
        {
            IDictionary<string, Object> transportHeaders = new Dictionary<string, Object>();
            transportHeaders.Add(TcpTransportHeader.RequestUri, this._remoteUriString);

            var callback = new SynchronizedRemotingCallback(this.SerializationFormat, returnType);
            this._handler.Call(this._remoteUri, TcpOperations.Request, transportHeaders, call, callback);

            try { callback.Wait(this.ExecutionTimeout); }
            finally { this._handler.Cancel(callback); }

            if (callback.Exception != null)
                throw callback.Exception is RemotingException
                    ? callback.Exception
                    : new RemotingException("remoting call error", callback.Exception);

            return callback.MethodReturn;
        }
    }
    /// <summary>proxy any type that for top-link.remoting rpc call, proxy based on .net build-in realproxy
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class DynamicProxy<T> : DynamicProxy where T : class
    {
        public DynamicProxy(Uri remoteUri
            , RemotingHandler handler) : base(typeof(T), remoteUri, handler) { }
        /// <summary>get real proxy object
        /// </summary>
        /// <returns></returns>
        public T GetProxy()
        {
            return this.GetTransparentProxy() as T;
        }
    }
}