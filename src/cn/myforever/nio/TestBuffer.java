package cn.myforever.nio;

import java.nio.ByteBuffer;

import org.junit.Test;
/**
 * 	一、缓冲区（Buffer）:在Java NIO中负责数据的存取。缓冲区就是数组，用于存储不同类型的数据
 * 	根据数据类型不同（boolean除外）了，提供了相应的缓冲区：
 *  BytrBuffer
 *  CharBuffer
 *  ShortBuffer
 *  IntBuffer
 *  LongBuffer
 *  FloatBuffer
 *  DoubleBuffer
 *	上述缓缓冲区的管理方式几乎一致，通过allocate()获取缓冲区
 *	
 *	二、缓冲区存取数据的两个核心方法：
 *	put():存入数据到缓冲区中
 *	get():获取缓冲区中的数据
 *	因为很多人都会混淆流，通道，缓冲区的读写方法，这里按我理解（不一定准确的解释，但是方便记），不管是通道还是流还是缓冲区，
 *	只要是从外面到里面，都是read,get，从里面到外面，write，put
 *	比如从硬盘到内存。从缓冲区到内存（其实都是内存，但是我觉得缓冲区在外面一层），都是read，get,而反过来就都是write,put
 *  
 *	三、缓冲区中的四个核心属性
 *	capacity:容量，表示缓冲区中最大存储数据 的容量，一旦声明不可改变
 *	limit：界限，表示缓冲区中可以操作数据的大小
 *	position：位置，表示缓冲区中正在操作数据的位置
 *	mark:标记，标记当前position的位置，可以通过reset()恢复德奥markd的位置
 *
 *	0 <= mark <= position <=limit <=capacity
 *	
 *	四、直接缓冲区与非直接缓冲区
 *	非直接缓冲区：通过allocate()方法分配缓冲区，将缓冲区建立在JVM的内存中
 *	直接缓冲区：通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在物理内存中，因为不需要再拷贝一份到JVM内存中，所以可以提高效率，但是
 *	也有确缺点，因为这部分内存JVM是不能操作的，驻留在常规的垃圾回收堆之外。
 *	
 */
public class TestBuffer {
	//缓冲区的基本操作
	@Test
	public void test1() {
		String str = "abcde";
		//1、分配一个指定大小的缓冲区，这里是1024字节，,我们用的最多的就是ByteBuffer
		//开始的时候是写数据模式
		ByteBuffer buf = ByteBuffer.allocate(1024);
		System.out.println("---------allocate()----------");
		//看一下各个属性的大小，理论上来说此事capacity因该为1024字节，看ByteBuffer的构造函数可以知道，limit也为1024.因为还没有数据，所以此事和position因该是0
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//2、利用put存入数据到缓冲区中(相当于从内存到缓冲区，从里面到外面用put)
		buf.put(str.getBytes());
		System.out.println("---------put()----------");
		//因为str是五个字节，说以存入后capacity应该还是1024，因为是写数据的模式，所以，limit还是1024,，position因为写了数据变成了5
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//3、上面都是写数据，接下来读数据的话需要先切换为读取数据的模式
		buf.flip();
		System.out.println("---------flip()----------");
		//flip()，将缓冲区改为了读模式，position变为了0，limit还是变为了5，capacity还是1024，因为改成了读取模式
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//4、利用get方法读取缓冲区中的数据（这里是从缓冲区到内存，所以相当于从外到里就用get）
		byte[] dst = new byte[buf.limit()];
		buf.get(dst);
		System.out.println("---------get()----------");
		//get()相当于读取数据，读取了5个，所以所以position变成了5，capacity还是1024，limit还是5
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//5、rewind()
		buf.rewind();
		System.out.println("---------rewind()----------");
		//rewind()相当于让用户可以重复读数据，所以。position变回了0，capacity还是1024，limit还是5
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//6、clear()
		buf.clear();
		System.out.println("---------clear()----------");
		//clear()相当于清空缓冲区，所以。position变回了0，capacity还是1024，limit变为1024
		System.out.println("capacity:"+buf.capacity());
		System.out.println("limit:"+buf.limit());
		System.out.println("position:"+buf.position());
		
		//6、这里就是相当于可以重新读了，用mark()来标记一下
		System.out.println("---------mark()----------");
		byte[] dst2 = new byte[buf.limit()];
		//先读取两个字节
		buf.get(dst2,0,2);
		System.out.println("ddst2:"+new String(dst,0,2));
		//此事position的位置应该是2
		System.out.println("postion:"+buf.position());
		//用mark()标记一下位置,position位置为2的位置
		buf.mark();
		//再去读俩个字节
		buf.get(dst2,2,2);
		//此时位置应该是4
		System.out.println("position:"+buf.position());
		//然后这里用reset()来恢复到mark的位置
		buf.reset();
		//此时位置应该变为2
		System.out.println("position:"+buf.position());
		
		//判断缓冲区中是否还有剩余的数据
		if(buf.hasRemaining()) {
			//查看还有多少个可操作的数据，因为位置变为了2，所以这里应该还有三个
			System.out.println(buf.remaining());
		}
		
		//创建一个直接缓冲区
		ByteBuffer buf2 = ByteBuffer.allocateDirect(1024);
		//用isDirect()方法来判断是直接缓冲区还是非直接缓冲区
		System.out.println(buf.isDirect());
		System.out.println(buf2.isDirect());
	}
	
	
	
	
}
