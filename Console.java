import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author Paulo Pagliosa
 */
public class Console
{
  private final static InputStreamReader keyboard;
  private final static BufferedReader in;
  private final static PrintStream out;

  static
  {
    keyboard = new InputStreamReader(System.in);
    in = new BufferedReader(keyboard);
    out = System.out;
  }

  private static void prompt(String message)
  {
    out.print(message + ": ");
  }

  public static void error(String message)
  {
    out.println("**Error: " + message + ". Try again");
  }

  private static void inputError()
  {
    error("input");
  }

  final public static void println(String s)
  {
    out.println(s);
  }

  final public static void println(int i)
  {
    out.println(i);
  }

  final public static void println(Object object)
  {
    out.println(object);
  }

  final public static void printf(String fmt, Object... args)
  {
    out.printf(fmt, args);
  }

  public static String readString(String message)
  {
    for (;;)
    {
      prompt(message);
      try
      {
        return in.readLine();
      }
      catch (IOException e)
      {
        inputError();
      }
    }
  }

  public static int readChar(String message)
  {
    for (;;)
    {
      prompt(message);
      try
      {
        return keyboard.read();
      }
      catch (IOException e)
      {
        inputError();
      }
    }
  }

  public static int readInt(String message)
  {
    for (;;)
      try
      {
        return Integer.parseInt(readString(message));
      }
      catch (NumberFormatException e)
      {
        error("integer expected");
      }
  }

  public static float readFloat(String message)
  {
    for (;;)
      try
      {
        return Float.parseFloat(readString(message));
      }
      catch (NumberFormatException e)
      {
        error("float expected");
      }
  }

  public static int readPositiveInt(String message)
  {
    for (;;)
    {
      int v = readInt(message);
      if (v > 0)
        return v;
      error("positive integer expected");
    }
  }

  public static float readPositiveFloat(String message)
  {
    for (;;)
    {
      float v = readFloat(message);
      if (v > 0f)
        return v;
      error("positive float expected");
    }
  }

  public static char readOption(String message, String options)
  {
    for (;;)
    {
      prompt(message);
      try
      {
        String input = in.readLine();
        int index = input.equals("") ? 0 : options.indexOf(input.charAt(0));

        if (index != -1)
          return options.charAt(index);
        error("invalid option");
      }
      catch (IOException e)
      {
        inputError();
      }
    }
  }

  public static Date readDate(String message)
  {
    for (DateFormat df = DateFormat.getDateInstance();;)
      try
      {
        return df.parse(readString(message));
      }
      catch (ParseException e)
      {
        error("invalide date format");
      }
  }

} // Console
