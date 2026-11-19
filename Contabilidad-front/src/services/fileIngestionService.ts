type FileKind = "SAP" | "EXTRACTO";

type CanonicalRow = {
  fecha: string;
  concepto: string;
  referencia: string;
  tipo: "D" | "C";
  valor: number;
};

type HeaderMap = {
  fecha?: number;
  concepto?: number;
  referencia?: number;
  tipo?: number;
  valor?: number;
  debito?: number;
  credito?: number;
};

type ExtractoLayoutConfig = {
  fallbackColumns?: {
    fecha: number;
    referencia: number;
    concepto: number;
    tipo?: number;
    valor?: number;
    debito?: number;
    credito?: number;
  };
};

type XlsxSheet = Record<string, unknown>;

type XlsxWorkbook = {
  SheetNames: string[];
  Sheets: Record<string, XlsxSheet>;
};

type XlsxRuntime = {
  read: (data: ArrayBuffer, options: { type: "array" }) => XlsxWorkbook;
  utils: {
    sheet_to_json: (
      sheet: XlsxSheet,
      options: { header: 1; raw: boolean; defval: string }
    ) => unknown[][];
  };
};

declare global {
  interface Window {
    XLSX?: XlsxRuntime;
  }
}

const XLSX_RUNTIME_URL = "https://cdn.sheetjs.com/xlsx-0.20.3/package/dist/xlsx.full.min.js";

const SUPPORTED_EXTENSIONS = [".csv", ".xls", ".xlsx"];

const EXTRACTO_LAYOUT_BY_BANK: Record<string, ExtractoLayoutConfig> = {
  // Estos layouts son ajustables por banco cuando se confirme estructura final por negocio.
  "BANCO REPUBLICA": {
    fallbackColumns: { fecha: 0, referencia: 1, concepto: 2, debito: 3, credito: 4 }
  },

  CITIBANK: {
    fallbackColumns: { fecha: 0, concepto: 1, referencia: 2, tipo: 3, valor: 4 }
  },
  "BANK OF AMERICA": {
    fallbackColumns: { fecha: 0, referencia: 1, concepto: 2, debito: 3, credito: 4 }
  }
};

const HEADER_ALIASES = {
  fecha: [
    "fecha",
    "fec",
    "fcontable",
    "fmov",
    "fecha movimiento",
    "fecha operacion",
    "fecha operación"
  ],
  concepto: [
    "concepto",
    "descripcion",
    "descripción",
    "detalle",
    "glosa",
    "narrativa",
    "descripcion movimiento",
    "descripcion transaccion"
  ],
  referencia: [
    "referencia",
    "documento",
    "doc",
    "num doc",
    "nro doc",
    "id movimiento",
    "consecutivo",
    "comprobante",
    "voucher",
    "numero",
    "nro"
  ],
  tipo: ["tipo", "naturaleza", "dc", "debito credito", "deb/cred", "d/c", "db/cr"],
  valor: ["valor", "monto", "importe", "valor usd", "valor movimiento", "total"],
  debito: ["debito", "debito usd", "debe", "cargo", "cargos", "deb"],
  credito: ["credito", "crédito", "haber", "abono", "abonos", "cred"]
};

const normalizeText = (value: string): string =>
  value
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toUpperCase()
    .replace(/\s+/g, " ")
    .trim();

const normalizeBankName = (value: string): string => normalizeText(value);

const normalizeCellText = (value: unknown): string => {
  if (value === null || value === undefined) {
    return "";
  }

  return String(value).trim();
};

const isSupportedFileName = (fileName: string): boolean => {
  const lower = fileName.toLowerCase();
  return SUPPORTED_EXTENSIONS.some((ext) => lower.endsWith(ext));
};

const parseDecimal = (value: unknown): number | null => {
  if (value === null || value === undefined) {
    return null;
  }

  if (typeof value === "number") {
    return Number.isFinite(value) ? value : null;
  }

  const raw = String(value).trim();

  if (raw.length === 0) {
    return null;
  }

  const compact = raw
    .replace(/\s+/g, "")
    .replace(/\$/g, "")
    .replace(/\u00A0/g, "")
    .replace(/[()]/g, "");

  const hasDot = compact.includes(".");
  const hasComma = compact.includes(",");

  let sanitized = compact;

  if (hasDot && hasComma) {
    const lastDot = compact.lastIndexOf(".");
    const lastComma = compact.lastIndexOf(",");

    // Si la ultima marca es coma, asumimos formato 1.234,56.
    if (lastComma > lastDot) {
      sanitized = compact.replace(/\./g, "").replace(/,/g, ".");
    } else {
      // Si la ultima marca es punto, asumimos formato 1,234.56.
      sanitized = compact.replace(/,/g, "");
    }
  } else if (hasComma) {
    sanitized = compact.replace(/\./g, "").replace(/,/g, ".");
  }

  const parsed = Number(sanitized);
  return Number.isFinite(parsed) ? parsed : null;
};

const normalizeTipo = (value: string): "D" | "C" | null => {
  const normalized = normalizeText(value);

  if (
    ["D", "DEBITO", "DEBIT", "DEBE", "CARGO", "DB"].includes(normalized) ||
    normalized.startsWith("DEB")
  ) {
    return "D";
  }

  if (
    ["C", "CREDITO", "CREDIT", "HABER", "ABONO", "CR"].includes(normalized) ||
    normalized.startsWith("CRE")
  ) {
    return "C";
  }

  return null;
};

const excelSerialToIsoDate = (serial: number): string => {
  // Excel day zero baseline adjustment for JS timestamp.
  const utcDays = Math.floor(serial - 25569);
  const utcValue = utcDays * 86400;
  const dateInfo = new Date(utcValue * 1000);
  const year = dateInfo.getUTCFullYear();
  const month = String(dateInfo.getUTCMonth() + 1).padStart(2, "0");
  const day = String(dateInfo.getUTCDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const normalizeDate = (value: unknown): string => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return excelSerialToIsoDate(value);
  }

  const raw = normalizeCellText(value);

  if (raw.length === 0) {
    return "";
  }

  const matchDmy = raw.match(/^(\d{1,2})[\/-](\d{1,2})[\/-](\d{2,4})$/);
  if (matchDmy !== null) {
    const day = String(Number(matchDmy[1])).padStart(2, "0");
    const month = String(Number(matchDmy[2])).padStart(2, "0");
    const year = matchDmy[3].length === 2 ? `20${matchDmy[3]}` : matchDmy[3];
    return `${year}-${month}-${day}`;
  }

  const matchYmd = raw.match(/^(\d{4})[\/-](\d{1,2})[\/-](\d{1,2})$/);
  if (matchYmd !== null) {
    const year = matchYmd[1];
    const month = String(Number(matchYmd[2])).padStart(2, "0");
    const day = String(Number(matchYmd[3])).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  return raw;
};

const matchesAlias = (header: string, aliases: string[]): boolean => {
  const normalized = normalizeText(header);
  return aliases.some((alias) => normalized.includes(normalizeText(alias)));
};

const detectHeaderRowIndex = (rows: unknown[][]): number => {
  const maxScan = Math.min(rows.length, 80);

  for (let rowIndex = 0; rowIndex < maxScan; rowIndex += 1) {
    const row = rows[rowIndex].map((cell) => normalizeCellText(cell));

    const score = [
      HEADER_ALIASES.fecha,
      HEADER_ALIASES.concepto,
      HEADER_ALIASES.referencia,
      [...HEADER_ALIASES.tipo, ...HEADER_ALIASES.debito, ...HEADER_ALIASES.credito],
      [...HEADER_ALIASES.valor, ...HEADER_ALIASES.debito, ...HEADER_ALIASES.credito]
    ].reduce((acc, aliases) => {
      const found = row.some((cell) => matchesAlias(cell, aliases));
      return found ? acc + 1 : acc;
    }, 0);

    if (score >= 3) {
      return rowIndex;
    }
  }

  return -1;
};

const buildHeaderMap = (headerRow: unknown[]): HeaderMap => {
  const headerMap: HeaderMap = {};

  headerRow.forEach((cell, index) => {
    const header = normalizeCellText(cell);

    if (header.length === 0) {
      return;
    }

    if (headerMap.fecha === undefined && matchesAlias(header, HEADER_ALIASES.fecha)) {
      headerMap.fecha = index;
      return;
    }

    if (headerMap.concepto === undefined && matchesAlias(header, HEADER_ALIASES.concepto)) {
      headerMap.concepto = index;
      return;
    }

    if (headerMap.referencia === undefined && matchesAlias(header, HEADER_ALIASES.referencia)) {
      headerMap.referencia = index;
      return;
    }

    if (headerMap.tipo === undefined && matchesAlias(header, HEADER_ALIASES.tipo)) {
      headerMap.tipo = index;
      return;
    }

    if (headerMap.valor === undefined && matchesAlias(header, HEADER_ALIASES.valor)) {
      headerMap.valor = index;
      return;
    }

    if (headerMap.debito === undefined && matchesAlias(header, HEADER_ALIASES.debito)) {
      headerMap.debito = index;
      return;
    }

    if (headerMap.credito === undefined && matchesAlias(header, HEADER_ALIASES.credito)) {
      headerMap.credito = index;
    }
  });

  return headerMap;
};

const resolveTipoValor = (
  row: unknown[],
  headerMap: HeaderMap,
  fallback?: ExtractoLayoutConfig["fallbackColumns"]
): { tipo: "D" | "C"; valor: number } | null => {
  const tipoIndex = headerMap.tipo ?? fallback?.tipo;
  const valorIndex = headerMap.valor ?? fallback?.valor;

  if (tipoIndex !== undefined && valorIndex !== undefined) {
    const tipo = normalizeTipo(normalizeCellText(row[tipoIndex]));
    const valor = parseDecimal(row[valorIndex]);

    if (tipo !== null && valor !== null) {
      return {
        tipo,
        valor: Math.abs(valor)
      };
    }

    // Fallback: cuando no existe tipo explicito, inferimos por signo del valor.
    if (tipo === null && valor !== null && valor !== 0) {
      return {
        tipo: valor >= 0 ? "D" : "C",
        valor: Math.abs(valor)
      };
    }
  }

  const debitoIndex = headerMap.debito ?? fallback?.debito;
  const creditoIndex = headerMap.credito ?? fallback?.credito;

  if (debitoIndex !== undefined || creditoIndex !== undefined) {
    const debito = debitoIndex !== undefined ? parseDecimal(row[debitoIndex]) : null;
    const credito = creditoIndex !== undefined ? parseDecimal(row[creditoIndex]) : null;

    const absDebito = debito === null ? 0 : Math.abs(debito);
    const absCredito = credito === null ? 0 : Math.abs(credito);

    if (absDebito > 0 && absCredito === 0) {
      return { tipo: "D", valor: absDebito };
    }

    if (absCredito > 0 && absDebito === 0) {
      return { tipo: "C", valor: absCredito };
    }
  }

  return null;
};

const toCanonicalRows = (
  rows: unknown[][],
  kind: FileKind,
  bancoNombre: string
): CanonicalRow[] => {
  const headerRowIndex = detectHeaderRowIndex(rows);
  const headerMap = headerRowIndex >= 0 ? buildHeaderMap(rows[headerRowIndex]) : {};

  const layout = EXTRACTO_LAYOUT_BY_BANK[normalizeBankName(bancoNombre)];
  const fallback =
    kind === "EXTRACTO"
      ? layout?.fallbackColumns
      : {
          fecha: 0,
          concepto: 1,
          referencia: 2,
          tipo: 3,
          valor: 4,
          debito: 3,
          credito: 4
        };

  const startIndex = headerRowIndex >= 0 ? headerRowIndex + 1 : 0;
  const dataRows = rows.slice(startIndex);

  const canonicalRows: CanonicalRow[] = [];

  dataRows.forEach((row, rowIndex) => {
    const fechaIndex = headerMap.fecha ?? fallback?.fecha ?? 0;
    const conceptoIndex = headerMap.concepto ?? fallback?.concepto ?? 1;
    const referenciaIndex = headerMap.referencia ?? fallback?.referencia ?? 2;

    const fecha = normalizeDate(row[fechaIndex]);
    const concepto = normalizeCellText(row[conceptoIndex]);
    const referenciaRaw = normalizeCellText(row[referenciaIndex]);

    const tipoValor = resolveTipoValor(row, headerMap, fallback);

    if (fecha.length === 0 && concepto.length === 0 && referenciaRaw.length === 0 && tipoValor === null) {
      return;
    }

    if (tipoValor === null) {
      return;
    }

    const referencia = referenciaRaw.length > 0 ? referenciaRaw : `AUTO-${rowIndex + 1}`;
    const fechaNormalizada = fecha.length > 0 ? fecha : "1900-01-01";
    const conceptoNormalizado = concepto.length > 0 ? concepto : "SIN CONCEPTO";

    canonicalRows.push({
      fecha: fechaNormalizada,
      concepto: conceptoNormalizado,
      referencia,
      tipo: tipoValor.tipo,
      valor: tipoValor.valor
    });
  });

  return canonicalRows;
};

const toCanonicalCsv = (rows: CanonicalRow[]): string => {
  const header = "fecha,concepto,referencia,tipo,valor";

  const body = rows
    .map((row) => {
      const escapedConcepto = row.concepto.replace(/"/g, '""');
      const escapedReferencia = row.referencia.replace(/"/g, '""');

      return [row.fecha, `"${escapedConcepto}"`, `"${escapedReferencia}"`, row.tipo, row.valor].join(",");
    })
    .join("\n");

  return body.length > 0 ? `${header}\n${body}` : header;
};

const parseCsvToMatrix = (content: string): unknown[][] => {
  const lines = content
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);

  return lines.map((line) => {
    const cells: string[] = [];
    let current = "";
    let inQuotes = false;

    for (let index = 0; index < line.length; index += 1) {
      const char = line[index];

      if (char === '"') {
        inQuotes = !inQuotes;
        continue;
      }

      if (char === "," && !inQuotes) {
        cells.push(current);
        current = "";
        continue;
      }

      current += char;
    }

    cells.push(current);
    return cells;
  });
};

const ensureXlsxRuntime = async (): Promise<XlsxRuntime> => {
  if (window.XLSX !== undefined) {
    return window.XLSX;
  }

  await new Promise<void>((resolve, reject) => {
    const existing = document.querySelector(`script[data-runtime='sheetjs']`) as HTMLScriptElement | null;

    if (existing !== null) {
      existing.addEventListener("load", () => resolve(), { once: true });
      existing.addEventListener("error", () => reject(new Error("No se pudo cargar el runtime XLSX")), {
        once: true
      });
      return;
    }

    const script = document.createElement("script");
    script.src = XLSX_RUNTIME_URL;
    script.async = true;
    script.dataset.runtime = "sheetjs";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("No se pudo cargar el runtime XLSX"));
    document.head.appendChild(script);
  });

  if (window.XLSX === undefined) {
    throw new Error("Runtime XLSX no disponible");
  }

  return window.XLSX;
};

export const normalizarArchivoCargue = async (
  file: File,
  kind: FileKind,
  bancoNombre: string
): Promise<string> => {
  if (!isSupportedFileName(file.name)) {
    throw new Error("Formato no soportado. Use CSV, XLS o XLSX");
  }

  const lowerName = file.name.toLowerCase();
  let matrix: unknown[][] = [];

  if (lowerName.endsWith(".csv")) {
    const content = await file.text();
    matrix = parseCsvToMatrix(content);
  } else {
    const runtime = await ensureXlsxRuntime();
    const buffer = await file.arrayBuffer();
    const workbook = runtime.read(buffer, { type: "array" });

    if (workbook.SheetNames.length === 0) {
      throw new Error("El archivo no contiene hojas para procesar");
    }

    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    matrix = runtime.utils.sheet_to_json(sheet, {
      header: 1,
      raw: true,
      defval: ""
    });
  }

  const canonicalRows = toCanonicalRows(matrix, kind, bancoNombre);

  if (canonicalRows.length === 0) {
    throw new Error("No se encontraron filas validas en el archivo");
  }

  return toCanonicalCsv(canonicalRows);
};
