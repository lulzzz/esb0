package com.artofarc.esb;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.artofarc.esb.context.Context;
import com.artofarc.esb.context.GlobalContext;
import com.artofarc.esb.context.PoolContext;
import com.artofarc.esb.context.WorkerPool;

public abstract class AbstractESBTest {

	protected Context context;

	@Before
	public void createContext() throws Exception {
		GlobalContext globalContext = new GlobalContext();
		// default WorkerPool
		globalContext.putWorkerPool(null, new WorkerPool(globalContext));

		context = new Context(new PoolContext(globalContext));
	}

	@After
	public void closeContext() throws Exception {
		if (context != null) {
			context.close();
			context.getPoolContext().close();
			context.getPoolContext().getGlobalContext().close();
		}
	}

   protected static byte[] readFile(String fileName) throws IOException {
      File file = new File(fileName);
      byte[] ba = new byte[(int) file.length()];
      try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
         dis.readFully(ba);
      }
      return ba;
   }

}
