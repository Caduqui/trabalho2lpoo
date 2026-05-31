import java.util.Scanner;

/**
 * Simple console helper for text-mode interaction.
 */
public class Console
{
  private final Scanner scanner = new Scanner(System.in);

  public String readLine(String prompt)
  {
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }

  public int readInt(String prompt)
  {
    while (true)
    {
      try
      {
        return Integer.parseInt(readLine(prompt));
      }
      catch (NumberFormatException e)
      {
        System.out.println("Entrada invalida. Digite um inteiro.");
      }
    }
  }

  public float readFloat(String prompt)
  {
    while (true)
    {
      try
      {
        return Float.parseFloat(readLine(prompt));
      }
      catch (NumberFormatException e)
      {
        System.out.println("Entrada invalida. Digite um numero.");
      }
    }
  }

  public void println(String msg)
  {
    System.out.println(msg);
  }

  public void printf(String fmt, Object... args)
  {
    System.out.printf(fmt, args);
  }

} // Console
