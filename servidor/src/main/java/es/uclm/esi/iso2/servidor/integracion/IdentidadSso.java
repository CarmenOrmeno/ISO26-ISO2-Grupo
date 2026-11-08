package es.uclm.esi.iso2.servidor.integracion;

/** Identidad corporativa que devuelve el SSO tras autenticar. */
public record IdentidadSso(String login, String nombreCompleto) {
}
