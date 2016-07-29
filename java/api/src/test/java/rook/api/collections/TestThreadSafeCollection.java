package rook.api.collections;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import rook.api.collections.ThreadSafeCollection;

public abstract class TestThreadSafeCollection {

	protected abstract ThreadSafeCollection<String> createCollection();
	
	@Test
	public void testAdd() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		assertEquals(c, "1", "2", "3");
	}
	
	@Test
	public void testAddDuplicate() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("2", true));
		assertEquals(c, "1", "2", "3", "2");
	}
	
	@Test
	public void testAddNoDuplicates() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", false));
		Assert.assertTrue(c.add("2", false));
		Assert.assertTrue(c.add("3", false));
		Assert.assertFalse(c.add("2", false));
		assertEquals(c, "1", "2", "3");
	}
	
	@Test
	public void testRemoveFirst() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("4", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("5", true));
		assertEquals(c, "1", "2", "3", "4", "3", "5");
		c.removeFirst("3");
		assertEquals(c, "1", "2", "4", "3", "5");
		c.removeFirst("1");
		assertEquals(c, "2", "4", "3", "5");
		c.removeFirst("4");
		assertEquals(c, "2", "3", "5");
		c.removeFirst("5");
		assertEquals(c, "2", "3");
		c.removeFirst("3");
		assertEquals(c, "2");
		c.removeFirst("2");
		assertEquals(c);
	}
	
	@Test
	public void testRemoveAll() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		assertEquals(c, "1", "2", "3", "1", "2");
		c.removeAll("2");
		assertEquals(c, "1", "3", "1");
		c.removeAll("3");
		assertEquals(c, "1", "1");
		c.removeAll("1");
		assertEquals(c);
	}
	
	@Test
	public void testRemoveIf() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("10", true));
		Assert.assertTrue(c.add("11", true));
		Assert.assertTrue(c.add("20", true));
		Assert.assertTrue(c.add("21", true));
		Assert.assertTrue(c.add("30", true));
		assertEquals(c, "10", "11", "20", "21", "30");
		c.removeIf(e-> e.startsWith("2"));
		assertEquals(c, "10", "11", "30");
		c.removeIf(e-> e.startsWith("3"));
		assertEquals(c, "10", "11");
		c.removeIf(e-> e.startsWith("1"));
		assertEquals(c);
	}
	
	@Test
	public void testPollHead() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("4", true));
		Assert.assertTrue(c.add("5", true));
		assertEquals(c, "1", "2", "3", "4", "5");
		Assert.assertEquals("1", c.pollHead());
		assertEquals(c, "2", "3", "4", "5");
		Assert.assertEquals("2", c.pollHead());
		assertEquals(c, "3", "4", "5");
		Assert.assertEquals("3", c.pollHead());
		assertEquals(c, "4", "5");
		Assert.assertEquals("4", c.pollHead());
		assertEquals(c, "5");
		Assert.assertEquals("5", c.pollHead());
		assertEquals(c);
		Assert.assertNull(c.pollHead());
	}
	
	@Test
	public void testPopTail() {
		ThreadSafeCollection<String> c = createCollection();
		Assert.assertTrue(c.add("1", true));
		Assert.assertTrue(c.add("2", true));
		Assert.assertTrue(c.add("3", true));
		Assert.assertTrue(c.add("4", true));
		Assert.assertTrue(c.add("5", true));
		assertEquals(c, "1", "2", "3", "4", "5");
		Assert.assertEquals("5", c.popTail());
		assertEquals(c, "1", "2", "3", "4");
		Assert.assertEquals("4", c.popTail());
		assertEquals(c, "1", "2", "3");
		Assert.assertEquals("3", c.popTail());
		assertEquals(c, "1", "2");
		Assert.assertEquals("2", c.popTail());
		assertEquals(c, "1");
		Assert.assertEquals("1", c.popTail());
		assertEquals(c);
		Assert.assertNull(c.popTail());
	}
	
	@SuppressWarnings("unchecked")
	private <T> void assertEquals(ThreadSafeCollection<T> c, T... elements) {
		Assert.assertEquals(elements.length, c.size());
		AtomicInteger iterated = new AtomicInteger(0);
		c.iterate((T element, int idx, int length) -> {
			Assert.assertEquals(elements[idx], element);
			iterated.getAndIncrement();
		});
		Assert.assertEquals(elements.length, iterated.get());
	}
}
