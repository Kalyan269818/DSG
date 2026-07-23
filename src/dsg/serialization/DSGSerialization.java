package dsg.serialization;

import java.util.Random;

public class DSGSerialization {

	/* Helpers */
	private static void print(String variant, byte[] bytes) {
		String string = String.format("%-15s %3d bytes", variant + ":", (bytes != null) ? bytes.length : 0);
		System.out.println(string);
	}
	
	private static void evaluate(boolean bool, int integer, String[] strings) {
		// Evaluate baseline
		DSGEvaluationBaseMessage baseline = new DSGEvaluationBaseMessage(bool, integer, strings);
		print("Baseline", DSGEvaluationBaseMessage.message2bytes(baseline));
		
		// Evaluate Externalizable-based serialization
		DSGExternalizableMessage externalizable = new DSGExternalizableMessage(bool, integer, strings);
		print("Externalizable", DSGExternalizableMessage.message2bytes(externalizable));
		
		// Evaluate data-stream-based serialization
		DSGDataStreamMessage data = new DSGDataStreamMessage(bool, integer, strings);
		print("Data stream", DSGDataStreamMessage.message2bytes(data));
		
		// Evaluate custom serialization
		DSGCustomMessage custom = new DSGCustomMessage(bool, integer, strings);
		print("Custom", DSGCustomMessage.message2bytes(custom));
	}

	/* Main */
	public static void main(String[] args) {
		// Evaluate empty message
		System.out.println("----- Empty Message -----");
		evaluate(false, 0, null);
		System.out.println();

		// Evaluate small message
		System.out.println("----- Small Message -----");
		evaluate(true, 47, new String[] { "DSG", "Assignment", "Task #2", "Evaluation" });
		System.out.println();

		// Evaluate large message
		System.out.println("----- Large Message -----");
		Random random = new Random(47);
		String[] strings = new String[10];
		for(int i = 0; i < strings.length; i++) {
			StringBuilder string = new StringBuilder(50);
			for(int j = 0; j < string.capacity(); j++) string.append(random.nextInt(10));
			strings[i] = string.toString();
		}
		evaluate(true, 10000, strings);
	}

}
