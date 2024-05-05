package nz.ac.auckland.se206.gpt;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /**
   * Retrieves the hint prompt based on the current game difficulty level. If the game difficulty is
   * set to EASY or MEDIUM, a predefined hint prompt is provided. For HARD difficulty, a specific
   * challenge prompt is given, instructing the AI not to provide any help.
   *
   * @return The hint prompt based on the current game difficulty level.
   */
  public static String getHint() {

    return "You are the dungeon presence of a digital escape room with a dungeon theme. Do not be"
        + " friendly to the user. Greet the user with a short taunt. Do not reveal you are an AI.";
  }

  /**
   * Generates a response prompt giving a contextual hint based on the current room if the user is
   * asking for a hint. Responds normally if the user is not asking for a hint.
   *
   * @param original The original user's message.
   * @param hint The contextual hint to be provided.
   * @return A response with a contextual hint or a normal response based on user's request.
   */
  public static String hintPrompt(String original, String hint) {
    // Gives a contextual hint based on the current room
    return "If the user is asking for a hint, give them a hint based"
        + " on the following: "
        + hint
        + " and make sure your response starts with the prefix \"Hint\" if you provide a hint."
        + " If the user is not asking for a hint, then respond normally. The user's response was:"
        + " \""
        + original
        + "\".";
  }

  /**
   * Generates a response prompt indicating that the user has run out of hints and taunts them.
   * Under no circumstances provides any information or answers to the user.
   *
   * @param original The original user's message.
   * @return A response indicating the user has no hints available.
   */
  public static String noHintPrompt(String original) {
    return "The user no long has hints available. Taunt them that they've run out of hints. Under"
        + " no circumstances should you offer any information or answers to the user. Under"
        + " no circumstances give the user the answer. The user's response was: \""
        + original
        + "\".";
  }
}
