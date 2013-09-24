import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.tokenizer.MorphAdornoSentenceTokenizer;


public class MorphAdornoSentenceTokenizerTest {

	@Test
	public void test() {
		MorphAdornoSentenceTokenizer st = new MorphAdornoSentenceTokenizer("This is a simple test.");
		System.out.println(st.getTokens());
		assertEquals("Token sequence isn't equal.", "[Token [pos=0, label=This], Token [pos=5, label=is], Token [pos=8, label=a], Token [pos=10, label=simple], Token [pos=17, label=test], Token [pos=21, label=.]]", st.getTokens().toString());
	}

}
