/** Representación visual de un estado: ícono de Material + etiqueta en español + clase CSS. */
export interface StatusPresentation {
  label: string;
  icon: string;
  cssClass: 'status-moving' | 'status-stopped' | 'status-alert' | 'status-serious' | 'status-unknown';
}
