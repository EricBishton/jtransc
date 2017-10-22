/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscAddIncludes;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.HashMap;
import java.util.Map;

@JTranscAddMembers(target = "d", value = "static {% CLASS java.lang.Thread %} _dCurrentThread; Thread thread;")
@JTranscAddMembers(target = "cs", value = "System.Threading.Thread _cs_thread;")
@JTranscAddIncludes(target = "cpp", cond = "USE_BOOST", value = {"thread", "map", "boost/thread.hpp", "boost/chrono.hpp"})
@JTranscAddIncludes(target = "cpp", value = {"thread", "map"})
@JTranscAddMembers(target = "cpp", cond = "USE_BOOST", value = "boost::thread t_;")
@JTranscAddMembers(target = "cpp", cond = "!USE_BOOST", value = "std::thread t_;")
//ThreadState is defined in Base.cpp, because it is used by other code before.
@JTranscAddMembers(target = "cpp", value = "ThreadState state = ThreadState::thread_in_java;")
//@JTranscAddMembers(target = "cpp", cond = "USE_BOOST", value = "static std::map<boost::thread::id, {% CLASS java.lang.Thread %}*> ###_cpp_threads;")
//@JTranscAddMembers(target = "cpp", cond = "!USE_BOOST", value = "static std::map<std::thread::id, {% CLASS java.lang.Thread %}*> ###_cpp_threads;")
@HaxeAddMembers({
	"private static var threadsMap = new haxe.ds.ObjectMap<Dynamic, {% CLASS java.lang.Thread %}>();",
	"#if cpp var _cpp_thread: cpp.vm.Thread; static var currentThread : cpp.vm.Tls<{% CLASS java.lang.Thread %}> = new Tls(); #end",
})
public class Thread implements Runnable {

	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	public static Thread currentThread() {
		//lazyPrepareThread();
		Thread out = _getCurrentThreadOrNull();
		//return (out != null) ? out : _mainThread;
		return out;
	}

	@JTranscMethodBody(target = "d", value = {
		"return _dCurrentThread;",
	})
	//@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "return _cpp_threads[boost::this_thread::get_id()];")
	//@JTranscMethodBody(target = "cpp", value = "return _cpp_threads[std::this_thread::get_id()];")
	@JTranscMethodBody(target = "cpp", value = "return N::getThreadEnv()->currentThread;")
	//@HaxeMethodBody(target = "cpp", value = "return threadsMap.get(cpp.vm.Thread.current().handle);")
	@HaxeMethodBody(target = "cpp", value = "return currentThread.get_value();")
	private static Thread _getCurrentThreadOrNull() {
		//for (Thread t : getThreadsCopy()) return t; // Just valid for programs with just once thread
		return null;
	}


	public StackTraceElement[] getStackTrace() {
		return new Throwable().getStackTrace();
	}

	@SuppressWarnings("unused")
	@JTranscMethodBody(target = "d", value = "Thread.yield();")
	//@JTranscMethodBody(target = "cpp", value = "std::this_thread::yield();")
	public static void yield() {
		try {
			Thread.sleep(1L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0));")
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "boost::this_thread::sleep_for(boost::chrono::milliseconds(p0));")
	@JTranscMethodBody(target = "cpp", value = "std::this_thread::sleep_for(std::chrono::milliseconds(p0));")
	public static void sleep(long millis) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0) + dur!(\"nsecs\")(p1));")
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "boost::this_thread::sleep_for(boost::chrono::milliseconds(p0));")
	@JTranscMethodBody(target = "cpp", value = "std::this_thread::sleep_for(std::chrono::milliseconds(p0));")
	//FIXME
	public static void sleep(long millis, int nanos) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	public Thread() {
		this(null, null, null, 1024);
	}

	//static private LinkedHashMap<Long, Thread> _threadsById;
	private ThreadGroup group;
	public String name;
	private long stackSize;
	private Runnable target;
	private int priority = MIN_PRIORITY;

	private long threadID;

	// Used for generating the unique thread ids for getId()
	private static long nextThreadID;

	private static synchronized long nextThreadID() {
		return ++nextThreadID;
	}


	// Used for naming anonymous threads
	private static long threadNameNumber;

	private static synchronized long nextThreadNumber() {
		return threadNameNumber++;
	}


	private UncaughtExceptionHandler uncaughtExceptionHandler = defaultUncaughtExceptionHandler;

	private boolean _isAlive;

	static private final Object staticLock = new Object();
	static private ThreadGroup _mainThreadGroup = null;
	static private Thread _mainThread = null;

	static {
		//_threadsById = new LinkedHashMap<>();
		// _mainThread will be set by `initMainThread
		//_threadsById.put(_mainThread.getId(), _mainThread);
	}

	public Thread(Runnable target) {
		this(null, target, null, 1024);
	}

	public Thread(ThreadGroup group, Runnable target) {
		this(group, target, null, 1024);
	}

	public Thread(String name) {
		this(null, null, name, 1024);
	}

	public Thread(ThreadGroup group, String name) {
		this(group, null, name, 1024);
	}

	public Thread(Runnable target, String name) {
		this(null, target, name, 1024);
	}

	public Thread(ThreadGroup group, Runnable target, String name) {
		this(group, target, name, 1024);
	}

	public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
		this.group = (group != null) ? group : currentThread().getThreadGroup();
		this.target = target;
		this.threadID = nextThreadID();
		this.name = (name != null) ? name : ("Thread-" + nextThreadNumber());
		this.stackSize = stackSize;
		//_init();
	}

	@JTranscMethodBody(target = "cpp", value = {
		//"asm(\"int $3\");",
		"N::getThreadEnv()->currentThread = this;",
	})
	@JTranscMethodBody(target = "d", value = {
		"this.thread = Thread.getThis();",
		"_dCurrentThread = this;"
	})
	@HaxeMethodBody(target = "cpp", value = "currentThread.set_value(this);" +
		"_cpp_thread = cpp.vm.Thread.current();")
	private void _initThreadEnv() {
	}

	/*synchronized static private Thread[] getThreadsCopy() {
		Collection<Thread> threads = getThreadSetInternal().values();
		synchronized (staticLock) {
			return threads.toArray(new Thread[0]);
		}
	}*/

	static private void initMainThread() {
		synchronized (staticLock) {
			if (JTranscSystem.isCpp() || JTranscSystem.isD() || JTranscSystem.isHaxeCpp()) {
				ThreadGroup mainThreadGroup = new ThreadGroup();
				new Thread(mainThreadGroup, "main")._initThreadEnv();
				return;
			}
			if (_mainThreadGroup == null) {
				_mainThreadGroup = new ThreadGroup();
			}
			if (_mainThread == null) {
				_mainThread = new Thread(_mainThreadGroup, "main");
				_mainThread._initThreadEnv();
			}
		}
	}

	//static private LinkedHashMap<Long, Thread> getThreadSetInternal() {
	//lazyPrepareThread();
	//	return _threadsById;
	//}

	public synchronized void start() {
		runInternalPreInit();
		_initThreadEnv();
		_start();
	}

	@JTranscMethodBody(target = "d", value = {
		"this.thread = new Thread(delegate () {",
		"	{% METHOD java.lang.Thread:runInternal:()V %}();",
		"});",
		"this.thread.start();"})
	@JTranscMethodBody(target = "cs", value = {
		"_cs_thread = new System.Threading.Thread(new System.Threading.ThreadStart(delegate() { this{% IMETHOD java.lang.Thread:runInternal:()V %}();  }));",
		"_cs_thread.Start();",
	})
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = {
		"t_ = std::thread(&{% SMETHOD java.lang.Thread:runInternalStatic:(Ljava/lang/Thread;)V %}, this);",
	})
	@JTranscMethodBody(target = "cpp", value = {
		"t_ = std::thread(&{% SMETHOD java.lang.Thread:runInternalStatic:(Ljava/lang/Thread;)V %}, this);",
	})
	@HaxeMethodBody(target = "cpp", value = "" +
		"var that = this;" +
		"cpp.vm.Thread.create(function():Void {" +
		//"	that._cpp_thread = cpp.vm.Thread.current();" +
		"	that{% IMETHOD java.lang.Thread:runInternal:()V %}();" +
		"});"
	)
	private void _start() {
		System.err.println("WARNING: Threads not supported! Executing thread code in the parent's thread!");
		runInternal();
	}

	@SuppressWarnings("unused")
	private void runInternal() {
		try {
			runInternalInit();
			_initThreadEnv();
			run();
		} catch (Throwable t) {
			uncaughtExceptionHandler.uncaughtException(this, t);
		} finally {
			runExit();
		}
	}

	@SuppressWarnings("unused")
	static private void runInternalStatic(Thread thread) {
		thread.runInternal();
	}


	@JTranscMethodBody(target = "cpp", value = "GC_init_pre_thread();")
	private void runInternalPreInitNative() {
	}

	private void runInternalPreInit() {
		runInternalPreInitNative();
		//final LinkedHashMap<Long, Thread> set = getThreadSetInternal();
		//synchronized (staticLock) {
		//set.put(getId(), this);
		_isAlive = true;
		//}
	}

	@JTranscMethodBody(target = "cpp", value = "GC_init_thread();")
	@HaxeMethodBody(target = "cpp", value = "threadsMap.set(_cpp_thread.handle, this);")
	private void runInternalInit() {
	}

	@JTranscMethodBody(target = "cpp", value = "GC_finish_thread();")
	@HaxeMethodBody(target = "cpp", value = "threadsMap.remove(_cpp_thread.handle); currentThread.set_value(null);")
	private void runInternalExit() {
	}

	private void runExit() {
		//final LinkedHashMap<Long, Thread> set = getThreadSetInternal();
		//synchronized (this) {
		runInternalExit();
		//	set.remove(getId());
		_isAlive = false;
		//}
	}

	@Override
	public void run() {
		if (this.target != null) {
			this.target.run();
		}
	}

	@Deprecated
	@JTranscMethodBody(target = "d", value = "this.thread.stop();")
	native public final void stop();

	@Deprecated
	public final synchronized void stop(Throwable obj) {
	}

	public void interrupt() {
	}

	public static boolean interrupted() {
		return Thread.currentThread().isInterrupted();
	}

	public boolean isInterrupted() {
		return false;
	}

	@Deprecated
	public void destroy() {
		throw new NoSuchMethodError();
	}

	public final boolean isAlive() {
		return _isAlive;
	}

	@Deprecated
	native public final void suspend();

	@Deprecated
	native public final void resume();

	public final void setPriority(int newPriority) {
		ThreadGroup group;
		if (newPriority > Thread.MAX_PRIORITY || newPriority < Thread.MIN_PRIORITY) {
			throw new IllegalArgumentException();
		}
		if ((group = getThreadGroup()) != null) {
			this.priority = Math.min(group.getMaxPriority(), newPriority);
			setPriorityNative(priority);
		}
	}

	public final void setPriorityNative(int newPriority) {
		// TODO implement me
	}

	public final int getPriority() {
		return priority;
	}

	public final synchronized void setName(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	public final ThreadGroup getThreadGroup() {
		return group;
	}

	public static int activeCount() {
		return currentThread().getThreadGroup().activeCount();
	}

	public static int enumerate(Thread tarray[]) {
		return currentThread().getThreadGroup().enumerate(tarray);
	}

	@Deprecated
	public int countStackFrames() {
		return 0;
	}

	public final synchronized void join(long millis) throws InterruptedException {
		join(millis, 0);
	}

	public final synchronized void join(long millis, int nanos) throws InterruptedException {
		final long start = System.currentTimeMillis();
		while (isAlive()) {
			final long current = System.currentTimeMillis();
			final long elapsed = current - start;
			if (elapsed >= millis) break;
			Thread.sleep(1L);
		}
	}

	public final void join() throws InterruptedException {
		while (isAlive()) {
			Thread.sleep(1L);
		}
	}

	native public static void dumpStack();

	private boolean _isDaemon = false;

	@JTranscMethodBody(target = "d", value = "this.thread.isDaemon = p0;")
	public final void setDaemonNative(boolean on) {
	}

	public final void setDaemon(boolean on) {
		if (isAlive()) {
			throw new IllegalThreadStateException();
		}
		setDaemonNative(on);
		_isDaemon = on;
	}

	//@JTranscMethodBody(target = "d", value = "return this.thread.isDaemon;")
	public final boolean isDaemon() {
		return _isDaemon;
	}

	native public final void checkAccess();

	public String toString() {
		ThreadGroup group = getThreadGroup();
		if (group != null) {
			return "Thread[" + getName() + "," + getPriority() + "," + group.getName() + "]";
		} else {
			return "Thread[" + getName() + "," + getPriority() + "," + "]";
		}
	}

	private ClassLoader classLoader = null;

	public ClassLoader getContextClassLoader() {
		if (this.classLoader == null) {
			this.classLoader = _ClassInternalUtils.getSystemClassLoader();
		}
		return this.classLoader;
	}

	public void setContextClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}

	public static boolean holdsLock(Object obj) {
		return false;
	}

	public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
		return new HashMap<Thread, StackTraceElement[]>();
	}

	public long getId() {
		return threadID;
	}

	public enum State {
		NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
	}

	public State getState() {
		return State.RUNNABLE;
	}

	public interface UncaughtExceptionHandler {
		void uncaughtException(Thread t, Throwable e);
	}

	static public UncaughtExceptionHandler defaultUncaughtExceptionHandler = (t, e) -> {
		System.out.println(t);
		System.out.println(e);
	};

	public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
		defaultUncaughtExceptionHandler = eh;
	}

	public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
		return defaultUncaughtExceptionHandler;
	}

	public UncaughtExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
		this.uncaughtExceptionHandler = eh;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
