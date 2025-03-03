package com.github.davidmoten.oas3.puml;

import com.github.davidmoten.junit.Asserts;
import com.github.davidmoten.oas3.internal.model.Throwables;
import org.junit.Test;

public class ConverterMainTest {

	@Test(expected = Throwables.class)
	public void testFileToPumlAndToUnsupportedFormats()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/openapi-example.yml",
		                             "target/demo-outputs/",
		                             Converter.UNSUPPORTED_FORMATS.toString()};
		ConverterMain.main(args);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFileToPumlAndToToSupportedFormatsNotYAML()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/log4j.properties",
		                             "target/demo-outputs/",
		                             Converter.SUPPORTED_FORMATS.toString()};
		ConverterMain.main(args);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFileToPumlAndToToSupportedFormatsNotOutputDirectory()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/openapi-example.yml",
		                             "src/test/resources/err.yml",
		                             Converter.SUPPORTED_FORMATS.toString()};
		ConverterMain.main(args);
	}

	@Test
	public void testFileToPuml()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/openapi-example.yml",
		                             "target/demo-outputs",
		                             "PUML"};
		ConverterMain.main(args);
	}

	@Test
	public void testFileToPumlAndToSVG()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/openapi-example.yml",
		                             "target/demo-outputs",
		                             "SVG"};
		ConverterMain.main(args);
	}

	@Test
	public void testFolderToPumlAndToSupportedFormats()
					throws
					Throwables {
		String[] args = new String[]{"src/test/resources/demo",
		                             "target/demo-outputs/demo",
		                             Converter.SUPPORTED_FORMATS.toString()};
		ConverterMain.main(args);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongNumberOfArgs()
					throws
					Throwables {
		ConverterMain.main(new String[]{"a"});
	}

	@Test
	public void isUtilityClass() {
		Asserts.assertIsUtilityClass(ConverterMain.class);
	}

}
