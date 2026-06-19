use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct EstadoNucleo {
    pub sincronizado: bool,
    pub confianza_global: u8,
    pub fecha_red: &'static str,
    pub hora_red: &'static str,
}

pub fn leer(_slot_solana: Option<u64>) -> EstadoNucleo {
    EstadoNucleo {
        sincronizado: true,
        confianza_global: 90,
        fecha_red: "DEMO",
        hora_red: "DEMO",
    }
}
