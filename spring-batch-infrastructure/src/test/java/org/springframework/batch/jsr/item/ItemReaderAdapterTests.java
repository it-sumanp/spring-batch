package org.springframework.batch.jsr.item;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.batch.api.chunk.ItemReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;

public class ItemReaderAdapterTests {

	private ItemReaderAdapter adapter;
	@Mock
	private ItemReader delegate;
	@Mock
	private ExecutionContext executionContext;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		adapter = new ItemReaderAdapter(delegate);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithNull() {
		adapter = new ItemReaderAdapter(null);
	}

	@Test
	public void testOpen() throws Exception {
		when(executionContext.get("ItemReader.reader.checkpoint")).thenReturn("checkpoint");

		adapter.open(executionContext);

		verify(delegate).open("checkpoint");
	}

	@Test(expected=ItemStreamException.class)
	public void testOpenException() throws Exception {
		when(executionContext.get("ItemReader.reader.checkpoint")).thenReturn("checkpoint");

		doThrow(new Exception("expected")).when(delegate).open("checkpoint");

		adapter.open(executionContext);
	}

	@Test
	public void testUpdate() throws Exception {
		when(delegate.checkpointInfo()).thenReturn("checkpoint");

		adapter.update(executionContext);

		verify(executionContext).put("ItemReader.reader.checkpoint", "checkpoint");
	}

	@Test(expected=ItemStreamException.class)
	public void testUpdateException() throws Exception {
		doThrow(new Exception("expected")).when(delegate).checkpointInfo();

		adapter.update(executionContext);
	}

	@Test
	public void testClose() throws Exception {
		adapter.close();

		verify(delegate).close();
	}

	@Test(expected=ItemStreamException.class)
	public void testCloseException() throws Exception {
		doThrow(new Exception("expected")).when(delegate).close();

		adapter.close();
	}

	@Test
	public void testRead() throws Exception {
		when(delegate.readItem()).thenReturn("item");

		assertEquals("item", adapter.read());
	}

	@Test
	@SuppressWarnings("serial")
	public void testCheckpointChange() throws Exception {
		ItemReaderAdapter adapter = new ItemReaderAdapter(new ItemReader() {

			private CheckpointContainer container = null;
			private List<String> items = new ArrayList<String>() {{
				add("foo");
				add("bar");
				add("baz");
			}};

			@Override
			public Object readItem() throws Exception {
				int index = container.getCount();

				if(index < items.size()) {
					container.setCount(index + 1);
					return items.get(index);
				} else {
					return null;
				}
			}

			@Override
			public void open(Serializable checkpoint) throws Exception {
				container = new CheckpointContainer();
			}

			@Override
			public void close() throws Exception {
			}

			@Override
			public Serializable checkpointInfo() throws Exception {
				return container;
			}
		});

		ExecutionContext context = new ExecutionContext();

		adapter.open(context);
		adapter.read();
		adapter.read();
		adapter.update(context);
		adapter.read();
		adapter.close();

		CheckpointContainer container = (CheckpointContainer) context.get("ItemReaderAdapterTests.1.reader.checkpoint");
		assertEquals(2, container.getCount());

	}

	public static class CheckpointContainer implements Serializable{
		private static final long serialVersionUID = 1L;
		private int count;

		public CheckpointContainer() {
			count = 0;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public String toString() {
			return "CheckpointContinaer has a count of " + count;
		}
	}
}
