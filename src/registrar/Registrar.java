package registrar;

public interface Registrar {
    public static void main(String[] args) {
            startRMIRegistry();
            String hostname = "localhost";
            String servicename = "RegistrarService";

            try{
                RegistrarInterface hello = new RegistrarInterface() {};
                Naming.rebind("rmi://" + hostname + "/" + servicename, hello);
                System.out.println("RMI Server successful started");
            }
            catch(Exception e){
                System.out.println("Server failed starting ...");
            }
        }

        public static void startRMIRegistry() {
            try{
                java.rmi.registry.LocateRegistry.createRegistry(1099);
                System.out.println("RMI Server ready");
            }
            catch(RemoteException e) {
                e.printStackTrace();
            }
        }
}
