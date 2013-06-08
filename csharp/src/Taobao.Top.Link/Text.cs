using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link
{
    //special log/error text here, easy support culture
    public class Text
    {
        public static string WS_REACH_MAX_IDLE = "reach max idle time";
        public static string WS_REACH_MAX_IDLE_AND_CLOSE = "reach maxIdleTimeSeconds=%s, close client channel";
                      
        public static string WS_HANDSHAKE_ERROR = "handshake error";
        public static string WS_HANDSHAKE_INVALID = "Invalid handshake response: %s";
                      
        public static string WS_CONNECT_ERROR = "connect error";
        public static string WS_CONNECT_FAIL = "connect fail";
        public static string WS_CONNECT_TIMEOUT = "connect timeout";
                      
        public static string WS_CHANNEL_CLOSED = "websocket channel closed";
        public static string WS_CONNECTION_CLOSED_BY = "connection closed: %s|%s";
        public static string WS_NOT_FINAL = "received a frame that not final fragment, not support!";
        public static string WS_SEND_ERROR = "send error";
                      
        public static string WS_ERROR_AT_CLIENT = "exceptionCaught at client";
        public static string WS_ERROR_AT_SERVER = "exceptionCaught at server";
                      
        public static string WS_SERVER_RUN = "server channel bind at %s";
        public static string WS_SERVER_STOP = "server channel shutdown";
                      
        public static string E_UNBIND_ERROR = "unbind error";
        public static string E_ID_DUPLICATE = "target identity can not equal itself";
        public static string E_CREATE_NEW = "create new EndpointProxy by identity";
        public static string E_UNKNOWN_MSG_FROM = "uknown message from";
        public static string E_GOT_ERROR = "got error: %s|%s";
        public static string E_CHANNEL_ERROR = "channel error";
        public static string E_ACCEPT = "%s accept a connect-in endpoint#%s and assign token#%s";
        public static string E_REFUSE = "refuse a connect-in endpoint";
        public static string E_CONNECT_SUCCESS = "sucessfully connect to endpoint#%s, and got token#%s";
        public static string E_NO_CALLBACK = "receive CONNECTACK, but no callback to handle it";
        public static string E_NO_SENDER = "do not have any valid channel to send";
        public static string E_EXECUTE_TIMEOUT = "execution timeout";
        public static string E_UNKNOWN_ERROR = "uknown error";
        public static string E_IDENTITY_NOT_MATCH_WITH_CALLBACK = "message's identity(%s) not match callback's(%s)";
        public static string E_SINGLE_ALLOW = "only allow sinle endpoint connected on a channel";
                      
        public static string RPC_POOL_BUSY = "channel pool is busy, retry later";
        public static string RPC_CAN_NOT_GET_CHANNEL = "can not get channel";
        public static string RPC_SEND_ERROR = "send error";
        public static string RPC_CALL_ERROR = "remoting call error";
        public static string RPC_EXECUTE_TIMEOUT = "remoting execution timeout";
        public static string RPC_CHANNEL_BROKEN = "channel broken with unknown error";
        public static string RPC_WAIT_INTERRUPTED = "waiting callback interrupted";
        public static string RPC_PENDING_CALL = "pending methodCall#%s";
        public static string RPC_GET_RETURN = "receive methodReturn of methodCall#%s";
        public static string RPC_RETURN_ERROR = "remote reutrn error#%s: %s";
                      
        public static string SCHEDULE_START = "scheduler start";
        public static string SCHEDULE_STOP = "scheduler stop";
        public static string SCHEDULE_GOT_MAX = "reach max task pending count %s";
        public static string SCHEDULE_TASK_REFUSED = "task refused";
        public static string SCHEDULE_TASK_DISPATCHED = "dispatch %s tasks";
        public static string SCHEDULE_DISPATCHER_DOWN = "dispatcher down! restarting...";
    }
}