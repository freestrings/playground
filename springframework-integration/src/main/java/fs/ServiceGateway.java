package fs;

public interface ServiceGateway {

    MessageA getA(String request);

    MessageB getB(String request);
}
