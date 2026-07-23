package dsg.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class DSGSerializationTests {

	/* Constants */
	public static final boolean BOOL = true;
	public static final int INTEGER = 47;
	public static final String[] STRINGS = { "DSG", "Assignment", "Task #2", "Evaluation" };

	/* Tests */
	@Test
	public void testBaseline() {
		// Serialize message
		DSGEvaluationBaseMessage original = new DSGEvaluationBaseMessage(BOOL, INTEGER, STRINGS);
		byte[] bytes = DSGEvaluationBaseMessage.message2bytes(original);
		assertNotNull(bytes, "message2bytes() returned null");
		
		// Deserialize message
		DSGEvaluationBaseMessage message = DSGEvaluationBaseMessage.bytes2message(bytes);
		assertEquals(original, message, "Deserialized message differs from the original");
	}
	
	@Test
	public void testExternalizable() {
		// Serialize message
		DSGExternalizableMessage original = new DSGExternalizableMessage(BOOL, INTEGER, STRINGS);
		byte[] bytes = DSGExternalizableMessage.message2bytes(original);
		assertNotNull(bytes, "message2bytes() returned null");
		
		// Deserialize message
		DSGExternalizableMessage message = DSGExternalizableMessage.bytes2message(bytes);
		assertEquals(original, message, "Deserialized message differs from the original");
	}
	
	@Test
	public void testDataStream() {
		// Serialize message
		DSGDataStreamMessage original = new DSGDataStreamMessage(BOOL, INTEGER, STRINGS);
		byte[] bytes = DSGDataStreamMessage.message2bytes(original);
		assertNotNull(bytes, "message2bytes() returned null");
		
		// Deserialize message
		DSGDataStreamMessage message = DSGDataStreamMessage.bytes2message(bytes);
		assertEquals(original, message, "Deserialized message differs from the original");
	}
	
	@Test
	public void testCustom() {
		// Serialize message
		DSGCustomMessage original = new DSGCustomMessage(BOOL, INTEGER, STRINGS);
		byte[] bytes = DSGCustomMessage.message2bytes(original);
		assertNotNull(bytes, "message2bytes() returned null");
		
		// Deserialize message
		DSGCustomMessage message = DSGCustomMessage.bytes2message(bytes);
		assertEquals(original, message, "Deserialized message differs from the original");
	}
	
}
