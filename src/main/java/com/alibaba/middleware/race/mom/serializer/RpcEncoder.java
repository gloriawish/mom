package com.alibaba.middleware.race.mom.serializer;






import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 对消息进行序列化操作
 * @author sei.zz
 *
 */
public class RpcEncoder extends MessageToByteEncoder{
	
	private Class<?> genericClass;
	private KryoSerialization kryo;
	public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
        kryo=new KryoSerialization();
        kryo.register(genericClass);
    }
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
			throws Exception {
		// TODO Auto-generated method stub
		
		//byte[] body=Tool.serialize(msg);
		//byte[] body=ByteObjConverter.ObjectToByte(msg);
		byte[] body=kryo.Serialize(msg);
		out.writeInt(body.length);
		out.writeBytes(body);
	}


}
