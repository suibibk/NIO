package cn.myforever.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

/**
 * 	一、通道（Channel）：由java.nio.channels包定义的。Channel表示IO源 与目标打开的连接。
 *  Channel类似于传统的“流”。只不过，Channel只能与Buffer进行交互，本身
 * 	 不存储数据，主要是配合缓冲区进行传输。
 * 
 * 	二、通道的主要实现
 * 	java.nio.channels.Channel 接口：
 * 		|--FileChannel   这个是文件传输
 * 		|--SocketChannel  TCP
 * 		|--ServerSocketChannel  TCP
 * 		|--DatagramChannel UDP
 * 
 * 	三、获取通道
 * 	1、Java针对支持通道的类提供了getChannel()方法
 * 		本地IO操作：FileInputStream/FileOutputStream、RandomAccessFile
 * 		网络IO操作：Socket、ServerSocket、DatagramSocket
 * 
 *  2、在JDK1.7中的NIO.2针对各个通道提供了静态方法open();
 *  3、在JDK1.7中的NIO.2的Files工具类的newByteChannel();
 *  
 *	四、通道之间的数据传输
 *	transferFrom()
 *  transferTo()
 */
public class TestChannel {
	//1、用通道实现一个文件的复制操作(非直接缓冲区)
	@Test
	public void test1() {
		//获取通道的流
		FileInputStream fis = null;
		FileOutputStream fos = null;
		//获取的通道
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			//获取文件流
			fis = new FileInputStream("1.txt");
			fos = new FileOutputStream("2.txt");
			//通过流获取通道
			inChannel = fis.getChannel();
			outChannel = fos.getChannel();
			
			//分配指定大小的缓冲区
			ByteBuffer buf  = ByteBuffer.allocate(1024);
			//将通道中的数据存入缓冲区，此事的缓冲区是写模式，按我之前的理解，从通道到缓冲区是从外到里所以用read
			while(inChannel.read(buf)!=-1) {
				//此时读完后将缓冲区切换为读取数据的模式
				buf.flip();
				//将缓冲区的数据写入通道中，此时相当于从里到外，用write
				outChannel.write(buf);
				//清空缓冲区,继续下一次读，其实这里只是把position的位置变为0，并没有真正清空缓冲区，数据是处于被遗忘的状态,此时limit也变成1024
				buf.clear();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(outChannel!=null) {
				try {
					outChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(inChannel!=null) {
				try {
					inChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
				
	}
	
	//2、使用直接缓冲区完成文件的复制
	@Test
	public void test2() {
		//这里不用流实现，直接用jdk1.7提供的open方法
		FileChannel inChannel =null;
		FileChannel outChannel =null;
		try {
			inChannel = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);
			//CREATE_NEW 这个表示存在报错，不存在就创建
			outChannel = FileChannel.open(Paths.get("3.txt"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);
			//到这里就可以直接跟例子1一样了，但是我们要用直接缓冲区，所以用map方法将内容映射到屋里内存当中
			
			//内存映射文件（这种方式只有ByteBuffer支持）
			MappedByteBuffer inMappedBuf = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
			//大小跟inChannel一样;这里是读写，所以上面获取通道的时候也需要是读写功能
			MappedByteBuffer outMappedBuf = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());
			//这种不用通道了，因为直接在物理内存中，直接操作缓冲区进行数据的读写操作即可
			byte[] bytes = new byte[inMappedBuf.limit()];
			inMappedBuf.get(bytes);
			//写入通道
			outMappedBuf.put(bytes);
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(outChannel!=null) {
				try {
					outChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(inChannel!=null) {
				try {
					inChannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
		//3、通道之间的数据传输
		@Test
		public void test3() {
			//这里不用流实现，直接用jdk1.7提供的open方法
			FileChannel inChannel =null;
			FileChannel outChannel =null;
			try {
				inChannel = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);
				//CREATE_NEW 这个表示存在报错，不存在就创建
				outChannel = FileChannel.open(Paths.get("4.txt"), StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE_NEW);
				//到这里就可以直接跟例子1一样了，但是我们要用直接缓冲区，所以用map方法将内容映射到屋里内存当中
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if(outChannel!=null) {
					try {
						outChannel.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(inChannel!=null) {
					try {
						inChannel.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
}
