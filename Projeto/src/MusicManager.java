class MusicManager {
  public static void main (String args[]) {
    try {
      // Criar servidor e correr.
      Server server = new Server(9091);
      server.run();
    }
    catch (Exception e) {
      System.out.println("Erro: " + e);
    }
  }
}
