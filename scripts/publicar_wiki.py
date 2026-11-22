"""Genera las páginas de la wiki de GitHub a partir de la documentación del repositorio.

Fuente única: docs/wiki/*.md (páginas), docs/casos-de-uso/CUxx.md (una página por CU) y DECISIONS.md
(se incrusta en la página Decisiones). Los enlaces relativos del repositorio se convierten al formato de la
wiki y lo que no es una página se enlaza al fichero en GitHub. Además se generan _Sidebar.md y _Footer.md.

Uso:  python3 scripts/publicar_wiki.py <carpeta_de_la_wiki_clonada>
Solo usa la biblioteca estándar. Borra las páginas .md de la carpeta destino antes de escribir las nuevas.
"""
import os
import re
import sys
from pathlib import Path

REPO = os.environ.get('GITHUB_REPOSITORY', 'CarmenOrmeno/ISO26-ISO2-Grupo')
RAMA = os.environ.get('RAMA_DOCUMENTACION', 'develop')
RAIZ = Path(__file__).resolve().parent.parent
WIKI = RAIZ / 'docs' / 'wiki'
CASOS = RAIZ / 'docs' / 'casos-de-uso'
ENLACE_MD = re.compile(r'(\]\()([^)\s]+)(\))')


def nombre_de_pagina(ruta: Path):
    """Nombre de la página de la wiki que corresponde a un fichero, o None si no es una página."""
    if ruta.suffix == '.md' and ruta.parent == WIKI:
        return ruta.stem
    if ruta.suffix == '.md' and ruta.parent == CASOS:
        return ruta.stem
    if ruta == RAIZ / 'DECISIONS.md':
        return 'Decisiones'
    return None


def convertir_enlaces(texto: str, origen: Path) -> str:
    """Reescribe los enlaces relativos de un fichero del repositorio para que funcionen en la wiki."""
    def convertir(m):
        destino = m.group(2)
        if re.match(r'^[a-z]+:', destino) or destino.startswith('#'):
            return m.group(0)
        ruta, _, ancla = destino.partition('#')
        absoluta = (origen.parent / ruta).resolve()
        pagina = nombre_de_pagina(absoluta)
        if pagina:
            nuevo = pagina + ('#' + ancla if ancla else '')
        else:
            relativa = absoluta.relative_to(RAIZ).as_posix()
            tipo = 'tree' if absoluta.is_dir() or destino.endswith('/') else 'blob'
            nuevo = f'https://github.com/{REPO}/{tipo}/{RAMA}/{relativa}' + ('#' + ancla if ancla else '')
        return m.group(1) + nuevo + m.group(3)
    return ENLACE_MD.sub(convertir, texto)


def pagina_caso_de_uso(fichero: Path) -> str:
    texto = fichero.read_text(encoding='utf-8')
    cu = fichero.stem
    titulo, _, cuerpo = texto.partition('\n')
    cabecera = (f'# 📄 {titulo.lstrip("# ").strip()}\n\n'
                f'[🏠 Inicio](Home) · [🎯 Requisitos y casos de uso](Requisitos-y-casos-de-uso) · '
                f'[🐙 Issue y sub-issues](https://github.com/{REPO}/issues?q=is%3Aissue+in%3Atitle+{cu})\n\n---\n')
    return cabecera + convertir_enlaces(cuerpo, fichero)


def pagina_decisiones() -> str:
    portada = convertir_enlaces((WIKI / 'Decisiones.md').read_text(encoding='utf-8'), WIKI / 'Decisiones.md')
    registro = (RAIZ / 'DECISIONS.md').read_text(encoding='utf-8').split('\n', 1)[1]
    registro = re.sub(r'^## (D-\d+)', r'## ⚖️ \1', registro, flags=re.M)
    return portada.rstrip() + '\n\n' + convertir_enlaces(registro, RAIZ / 'DECISIONS.md')


def barra_lateral(casos) -> str:
    lista_cu = ' · '.join(f'[{c}]({c})' for c in casos)
    return f'''### 🏥 Radiodiagnóstico SESCAM

**[🏠 Inicio](Home)**

**📋 Proyecto**
- [👥 Equipo y roles](Equipo-y-roles)
- [📝 Actas de reuniones](Actas-de-reuniones)
- [📅 Planificación](Planificacion)
- [⚖️ Decisiones](Decisiones)

**🎯 Requisitos**
- [🎯 Requisitos y casos de uso](Requisitos-y-casos-de-uso)
- 📄 {lista_cu}

**🏛️ Diseño**
- [🏛️ Arquitectura](Arquitectura)

**🛠️ Procesos**
- [🔀 Gestión de configuración](Plan-de-gestion-de-configuracion)
- [🛡️ Plan de calidad](Plan-de-calidad)
- [🧪 Plan de pruebas](Plan-de-pruebas)

**🪞 Cierre**
- [🪞 Autoevaluación y experiencia](Autoevaluacion-y-experiencia)

---
🐙 [Repositorio](https://github.com/{REPO}) · [Issues](https://github.com/{REPO}/issues) · [Versiones](https://github.com/{REPO}/releases)
'''


def pie() -> str:
    return (f'🏥 **Radiodiagnóstico SESCAM** · Ingeniería del Software II · UCLM 2026/27 · '
            f'Generada desde [`docs/wiki`](https://github.com/{REPO}/tree/{RAMA}/docs/wiki) en `{RAMA}`: '
            f'los cambios se hacen allí, no en esta wiki.\n')


def publicar(destino: Path) -> list:
    for viejo in destino.glob('*.md'):
        viejo.unlink()
    escritas = {}
    for fichero in sorted(WIKI.glob('*.md')):
        if fichero.name != 'Decisiones.md':
            escritas[fichero.name] = convertir_enlaces(fichero.read_text(encoding='utf-8'), fichero)
    escritas['Decisiones.md'] = pagina_decisiones()
    casos = sorted(f.stem for f in CASOS.glob('CU*.md'))
    for cu in casos:
        escritas[f'{cu}.md'] = pagina_caso_de_uso(CASOS / f'{cu}.md')
    escritas['_Sidebar.md'] = barra_lateral(casos)
    escritas['_Footer.md'] = pie()
    for nombre, contenido in escritas.items():
        (destino / nombre).write_text(contenido, encoding='utf-8')
    return sorted(escritas)


if __name__ == '__main__':
    if len(sys.argv) != 2 or not Path(sys.argv[1]).is_dir():
        sys.exit('Uso: python3 scripts/publicar_wiki.py <carpeta_de_la_wiki_clonada>')
    for pagina in publicar(Path(sys.argv[1])):
        print('publicada', pagina)
