package com.alibaba.middleware.race.mom.serializer;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder{
	private Class<?> genericClass;
	private KryoSerialization kryo;
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
        kryo=new KryoSerialization();
        kryo.register(genericClass);
    }
	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("decode:"+in.readableBytes());
		int HEAD_LENGTH=4;
        if (in.readableBytes() < HEAD_LENGTH) {
            return;
        }
        in.markReaderIndex();               
        int dataLength = in.readInt();       
        if (dataLength < 0) { 				 
            ctx.close();
        }
        if (in.readableBytes() < dataLength) { 
            in.resetReaderIndex();
            return;
        }
        byte[] body = new byte[dataLength]; 
        in.readBytes(body);  				
        
        
        //Object obj=Tool.deserialize(body, genericClass);
        //Object obj=ByteObjConverter.ByteToObject(body);
        Object obj=kryo.Deserialize(body);
        out.add(obj);
	}

}
