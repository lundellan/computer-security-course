package server;

public class UnautherizedException extends Exception {
    
  public UnautherizedException() { 
      super(); 
    }
    public UnautherizedException(String message) { 
      super(message); 
    }
    public UnautherizedException(String message, Throwable cause) { 
      super(message, cause); 
    }
    public UnautherizedException(Throwable cause) { 
      super(cause); 
    }
  }