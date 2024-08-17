package org.cis1200;

import java.util.*;

/**
 * A Markov Chain is a data structure that tracks the frequency with which one
 * token follows another token in a collection of sequences. This project uses a
 * Markov Chain to model tweets by gathering the frequency information from a
 * Twitter feed. We can use the MarkovChain to generate "plausible" tweets by
 * conducting a random walk through the chain according to the frequencies.
 * Please see the homework instructions for more information on Markov Chains.
 * <p>
 * <b>TRAINING:</b>
 * <p>
 * An ILLUSTRATIVE EXAMPLE (see the corresponding test cases throughout the
 * project files). Suppose we want to train the MarkovChain on these two
 * {@code String}s that represent tweets:
 * <p>
 * {@code "a table and a chair"} and
 * <p>
 * {@code "a banana! and a banana?"}
 * <p>
 * We first <i>parse</i> these two tweets into sequences of {@code String}
 * <i>tokens</i> that represent the individual words and punctuation marks
 * of the tweet. (See {@link TweetParser#parseAndCleanTweet(String)}.)
 * For these two tweets, we get the lists below. (Note how
 * the punctuation is separated as individual tokens.)
 * <br>
 * {@code ["a", "table", "and", "a", "chair"]}
 * <br>
 * {@code ["a", "banana", "!", "and", "a", "banana", "?"]}
 * </p>
 * <p>
 * The {@code MarkovChain} that results from this training data maps each
 * observed
 * token to a {@code ProbabilityDistribution} that is based on the recorded
 * occurrences
 * of bigrams (adjacent tokens) in the data. The {@code MarkovChain} also
 * computes a {@code ProbabilityDistribution} that contains the frequencies with
 * which words
 * start the tweets of the training data.
 * <p>
 * If we print the Markov Chain resulting from the training data above, we see:
 *
 * <pre>
 *  ILLUSTRATIVE EXAMPLE MARKOV CHAIN:
 *  startTokens: { "a":2 }
 *  bigramFrequencies:
 *  "!":    { "and":1 }
 *  "?":    { "&lt;END&gt;":1 }
 *  "a":    { "banana":2  "chair":1  "table":1 }
 *  "and":  { "a":2 }
 *  "banana":   { "!":1  "?":1 }
 *  "chair":    { "&lt;END&gt;":1 }
 *  "table":    { "and":1 }
 * </pre>
 * <p>
 * For this training data, because both tweets start with "a", the
 * {@code startTokens}
 * distribution simply records that fact.
 * <p>
 * The {@code bigramFrequencies} data structure records the information about
 * occurrences of adjacent tokens in the tweets. For instance,
 * the token "a" is followed by "banana" twice, "chair" once, and "table" once.
 * The token "!" is followed by "and" once, whereas "?" (like "chair") appears
 * only at the end of a tweet.
 *
 * <p>
 * NOTE: we use the {@code END_TOKEN} marker {@code "<END>"} to mark the end of
 * a tweet.
 *
 * <p>
 * <b>SAMPLING</b>
 * <p>
 * Once we have trained the Markov Chain, we can use it to generate new
 * sequences that
 * mimic the training inputs. This is done by conducting a "random" walk through
 * the chain.
 * <p>
 * The walk begins by choosing a token from the {@code startTokens}
 * distribution.
 * In the running example, since both tweets start with the token "a", the only
 * possible starting token is "a".
 * <p>
 * Then, a sequence of tokens is generated by choosing the next symbol according
 * to
 * the bigram distributions until the {@code END_TOKEN} is encountered, at which
 * point the walk
 * is finished. For example. after the token "a", the walk might pick "chair",
 * and then,
 * because "chair" is always followed by the {@code END_TOKEN}, the walk is
 * complete. A different
 * walk might yield the sequence "a table and a banana?"
 * <p>
 * We model this random walk process as an {@code Iterator} that, given a source
 * of
 * (random) numbers, yields the sequence of tokens visited by choosing that
 * "path" through
 * the Markov Chain, as explained in more detail below.
 * <p>
 *
 * Your job is to complete the code in this class to provide the functionality
 * described above.
 *
 */
public class MarkovChain {

    /** probability distribution of initial words in a sentence */
    final ProbabilityDistribution<String> startTokens;

    /** for each word, probability distribution of next word in a sentence */
    final Map<String, ProbabilityDistribution<String>> bigramFrequencies;

    /** end of sentence marker */
    static final String END_TOKEN = "<END>";

    /**
     * Construct an empty {@code MarkovChain} that can later be trained.
     *
     * This constructor is implemented for you.
     */
    public MarkovChain() {
        this.bigramFrequencies = new TreeMap<>();
        this.startTokens = new ProbabilityDistribution<>();
    }

    /**
     * Construct a trained {@code MarkovChain} from the given list of training data.
     * The training data is assumed to be non-null. Uses the {@link #addSequence}
     * method
     * on each of the provided sequences. (It is recommended that you implement
     * {@link #addBigram} and {@link #addSequence} first.)
     *
     * @param trainingData - the input sequences of tokens from which to construct
     *                     the {@code MarkovChain}
     */
    public MarkovChain(List<List<String>> trainingData) {
        this.bigramFrequencies = new TreeMap<>();
        this.startTokens = new ProbabilityDistribution<>();
        for (List<String> lString : trainingData) {
            Iterator<String> it = lString.iterator();
            addSequence(it);
        }
    }

    /**
     * Adds a bigram to the Markov Chain information by
     * recording it in the appropriate probability distribution
     * of {@code bigramFrequencies}. (If this is the first time that {@code first}
     * has appeared in a bigram, creates a new probability distribution first.)
     *
     * @param first  The first word of the Bigram (should not be null)
     * @param second The second word of the Bigram (should not be null)
     * @throws IllegalArgumentException - when either parameter is null
     */
    void addBigram(String first, String second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException();
        }

        if (bigramFrequencies.isEmpty() || !bigramFrequencies.containsKey(first)) {
            bigramFrequencies.put(first, new ProbabilityDistribution<>());
            ProbabilityDistribution<String> pD = bigramFrequencies.get(first);
            pD.record(second);
        } else {
            ProbabilityDistribution<String> pD = bigramFrequencies.get(first);
            pD.record(second);
        }
    }

    /**
     * Adds a single tweet's training data to the Markov Chain frequency
     * information, by:
     *
     * <ol>
     * <li>recording the first token in {@code startTokens}
     * <li>recording each subsequent bigram of co-occurring pairs of tokens
     * <li>recording a final bigram of the last token of the {@code tweet} and
     * {@code END_TOKEN} to mark the end of the sequence
     * </ol>
     * <p>
     * Does nothing if the tweet is empty.
     *
     * <p>
     * Note that this is where you <i>train</i> the MarkovChain.
     *
     * @param tweet an iterator representing one tweet of training data
     * @throws IllegalArgumentException when the tweet Iterator is null
     */
    public void addSequence(Iterator<String> tweet) {

        if (tweet == null) {
            throw new IllegalArgumentException();
        }

        String start = null;
        while (tweet.hasNext()) {
            String next = tweet.next();
            if (start == null) {
                startTokens.record(next);
            } else {
                addBigram(start, next);
            }
            start = next;
        }

        if (start != null) {
            addBigram(start, END_TOKEN);
        }

    }

    /**
     * Returns the ProbabilityDistribution for a given token. Returns null if
     * none exists. This function is implemented for you.
     *
     * @param token - the token for which the ProbabilityDistribution is sought
     * @throws IllegalArgumentException - when parameter is null.
     * @return a ProbabilityDistribution or null
     */
    ProbabilityDistribution<String> get(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token cannot be null.");
        }

        return bigramFrequencies.get(token);
    }

    /**
     * Gets a walk through the Markov Chain that follows
     * the path given by the {@code NumberGenerator}. See
     * {@link MarkovChainIterator} for the details.)
     *
     * This function is implemented for you.
     *
     * @param ng the path to follow (represented as a {@code NumberGenerator}
     * @return an {@code Iterator} that yields the tokens on that path
     *
     */
    public Iterator<String> getWalk(NumberGenerator ng) {
        return new MarkovChainIterator(ng);
    }

    /**
     * Gets a random walk through the Markov Chain.
     *
     * This function is implemented for you.
     *
     * @return an {@code Iterator} that yields the tokens on that path
     */
    public Iterator<String> getRandomWalk() {
        return getWalk(new RandomNumberGenerator());
    }

    /**
     * This inner class represents a "walk" through the Markov Chain.
     * as an {@code Iterator} that yields each token encountered during the walk.
     * <p>
     * A walk through the chain is determined by the given {@code NumberGenerator},
     * which picks from among the choices of tokens according to their
     * probability distributions.
     * <p>
     * For example, given
     * 
     * <pre>
     *  ILLUSTRATIVE EXAMPLE MARKOV CHAIN:
     *  startTokens: { "a":2 }
     *  bigramFrequencies:
     *  "!":    { "and":1 }
     *  "?":    { "&lt;END&gt;":1 }
     *  "a":    { "banana":2  "chair":1  "table":1 }
     *  "and":  { "a":2 }
     *  "banana":   { "!":1  "?":1 }
     *  "chair":    { "&lt;END&gt;":1 }
     *  "table":    { "and":1 }
     * </pre>
     * 
     * The sequence of numbers 0 2 0 determines the (valid) walk consisting of the
     * three tokens
     * "a", "chair", and {@code END_TOKEN} as follows:
     * <ul>
     * <li>The first 0 picks out "a" from among the {@code startTokens}. (Since "a"
     * occurred
     * with frequency 2, either 0 or 1 would yield "a".)
     * <li>Next, the 2, picks out "chair" from the probability distribution over
     * bigrams
     * associated with "a", because 0-1 map to "banana", 2 maps to "chair", and 3
     * maps to
     * "table".
     * <li>Finally, the last 0 picks out {@code END_TOKEN} from the bigrams
     * associated with
     * "chair".
     * </ul>
     * See the documentation for {@code pick} in
     * {@link ProbabilityDistribution#pick(int)}
     * for more details.
     */
    class MarkovChainIterator implements Iterator<String> {
        // stores the source of numbers that determine the path of ths walk
        private NumberGenerator ng;
        private String currWord;

        // this (MarkovChainIterator) is an inner class
        // so it can access the field of the outer class (MarkovChain)

        /**
         * Constructs an iterator that follows the path specified by the given
         * {@code NumberGenerator}.The first token of the walk is chosen from
         * {@code startTokens}
         * by picking from that distribution using ng's first number. If the number
         * generator can
         * not provide a valid start index, or if there are no start tokens, returns an
         * empty
         * Iterator (i.e., one for which hasNext is always false).
         * 
         * @param ng the number generator to use for this walk
         */
        MarkovChainIterator(NumberGenerator ng) {

            this.ng = ng;
            if (startTokens == null || startTokens.getRecords().isEmpty()) {
                currWord = END_TOKEN;
            } else {
                try {
                    this.currWord = startTokens.pick(this.ng);

                } catch (IllegalArgumentException | NoSuchElementException e) {
                    this.currWord = null;
                }
            }
        }

        /**
         * This method determines whether there is a next token in the
         * Markov Chain based on the current state of the walk. Remember that the
         * end of a sentence is denoted by the token {@code END_TOKEN}.
         * <p>
         * Your solution should be very short.
         *
         * @return true if {@link #next()} will return a non-{@code END_TOKEN} String
         *         and false otherwise
         */
        @Override
        public boolean hasNext() {
            return currWord != null && !currWord.equals(END_TOKEN);
        }

        /**
         *
         * @return the next word in the MarkovChain's walk
         * @throws NoSuchElementException if there are no more words on the walk
         *                                through the chain (i.e. it has reached
         *                                {@code END_TOKEN}),
         *                                or if the number generator provides an invalid
         *                                choice
         *                                (e.g, an illegal argument for {@code pick}).
         */
        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String prevWord = currWord;
            // catch the exception
            currWord = bigramFrequencies.get(currWord).pick(ng);
            return prevWord;
        }

    }

    /**
     * Generate a list of numbers such that if it is installed as the
     * number generator for the MarkovChain, and used as an iterator,
     * the words returned in sequence will be the list of provided words.
     *
     * Note that the length of the list of numbers is equal to the length
     * of the list of words plus one (for the {@code END_TOKEN}).
     *
     * This function is implemented for you.
     *
     * @param words an ordered list of words that the MarkovChain should generate
     *
     * @return a list of integers representing a walk through the Markov Chain that
     *         produces the given sequence of words
     *
     * @throws IllegalArgumentException when any of the following are true:
     *                                  <ul>
     *                                  <li>{@code words} is null or empty
     *                                  <li>the first word in the list is not in
     *                                  {@code startTokens}
     *                                  <li>any of the words in the list is not
     *                                  found as a key in the chain
     *                                  <li>if the last word of the list cannot
     *                                  transition to {@code END_TOKEN}
     *                                  </ul>
     */
    public List<Integer> findWalkChoices(List<String> words) {
        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("Invalid empty or null words");
        }
        words.add(END_TOKEN);
        List<Integer> choices = new LinkedList<>();

        String curWord = words.remove(0);
        choices.add(startTokens.index(curWord));

        while (words.size() > 0) {
            ProbabilityDistribution<String> curDist = bigramFrequencies.get(curWord);
            String nextWord = words.remove(0);
            choices.add(curDist.index(nextWord));
            curWord = nextWord;
        }
        return choices;
    }

    /**
     * Use this method to print out markov chains with words and probability
     * distributions.
     *
     * This function is implemented for you.
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("startTokens: ").append(startTokens.toString());
        res.append("\nbigramFrequencies:\n");
        for (Map.Entry<String, ProbabilityDistribution<String>> c : bigramFrequencies.entrySet()) {
            res.append("\"");
            res.append(c.getKey());
            res.append("\":\t");
            res.append(c.getValue().toString());
            res.append("\n");
        }
        return res.toString();
    }

}