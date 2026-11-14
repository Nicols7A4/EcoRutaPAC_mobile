package pe.edu.usat.ecorutapac.data.remote.dto.response;

public class VehiculoItem {
    private int id;
    private String placa;
    private String tipo;
    private String capacidad;

    public VehiculoItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCapacidad() { return capacidad; }
    public void setCapacidad(String capacidad) { this.capacidad = capacidad; }

    @Override
    public String toString() {
        return placa + " (" + tipo + ")";
    }
}
