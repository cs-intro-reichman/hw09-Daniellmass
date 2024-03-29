import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }
    


    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String window = "";
        char c = 0;
        In in = new In(fileName);
        for (int i = 0; i < windowLength && !in.isEmpty(); i++) {
            window += in.readChar();
        }
        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window = window.substring(1) + c;
        }
        in.close();
        for ( List probs :CharDataMap.values()) {
           calculateProbabilities(probs); 
        }
	}
     

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {
        if (probs == null) {
         throw new IllegalArgumentException();
        }
		int totalChar = 0;
        double cpProb = 0;
        ListIterator iterate = probs.listIterator(0);
        while (iterate.hasNext()) {
            CharData current = iterate.next();
            totalChar += current.count;
        }
        iterate = probs.listIterator(0);
        while (iterate.hasNext()) {
        CharData current = iterate.next();
        current.p = (double) current.count / totalChar;
        cpProb += current.p;
        current.cp = cpProb;
        }
	}
    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
     if (probs == null) {
      throw new IllegalArgumentException();
     }
     double r = randomGenerator.nextDouble();
     double totalProb = 0;
     ListIterator iterate = probs.listIterator(0);
     while (iterate.hasNext()) {
      CharData current = iterate.next();
      totalProb += current.cp;
      if (current.cp >= r) {
        return current.chr;
      }
     }  
    return probs.get(probs.getSize() - 1).chr;
    }


    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }
        StringBuilder genText = new StringBuilder(initialText);
        while (genText.length() < textLength + initialText.length()) {
            String currWindow = genText.substring(Math.max(0, genText.length() - windowLength));
            List charDataL = CharDataMap.get(currWindow);
            if (charDataL == null) {
              break;
            }
             char nextCh = getRandomChar(charDataL);
              genText.append(nextCh);  
        }
        return genText.toString();
	}


    

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {

    }
}
