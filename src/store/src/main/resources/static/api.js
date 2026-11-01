export const endpointCatalog = [
  ['GET', '/products', 'Список товаров'], ['GET', '/products/{id}', 'Карточка товара'],
  ['POST', '/products', 'Создание товара'], ['PATCH', '/products/{id}/stock', 'Списание остатков'], ['DELETE', '/products/{id}', 'Удаление товара'],
  ['GET', '/clients', 'Список клиентов и пагинация'], ['GET', '/clients/search', 'Поиск по имени и фамилии'], ['GET', '/clients/{id}', 'Карточка клиента'],
  ['POST', '/clients', 'Создание клиента'], ['PATCH', '/clients/{id}/address', 'Изменение адреса клиента'], ['DELETE', '/clients/{id}', 'Удаление клиента'],
  ['GET', '/suppliers', 'Список поставщиков'], ['GET', '/suppliers/{id}', 'Карточка поставщика'], ['POST', '/suppliers', 'Создание поставщика'],
  ['PATCH', '/suppliers/{id}/address', 'Изменение адреса поставщика'], ['DELETE', '/suppliers/{id}', 'Удаление поставщика'],
  ['POST', '/images', 'Загрузка изображения'], ['GET', '/images/{id}', 'Изображение по ID'], ['GET', '/images/product/{productId}', 'Изображение товара'],
  ['PUT', '/images/{id}', 'Замена изображения'], ['DELETE', '/images/{id}', 'Удаление изображения'],
];
export const history = [];
export const usedEndpoints = new Set();
export function endpointKey(method, path) {
  const clean = path.split('?')[0];
  return endpointCatalog.find(([m, template]) => m === method && new RegExp('^' + template.replace(/\{[^}]+\}/g, '[^/]+') + '$').test(clean))?.slice(0, 2).join(' ');
}
export async function request(path, { method = 'GET', body, binary = false } = {}) {
  const started = performance.now();
  const record = { method, path, at: new Date(), status: 0, duration: 0, response: null };
  try {
    const response = await fetch('/api/v1' + path, {
      method, headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(15000),
    });
    record.status = response.status;
    let data;
    if (binary && response.ok) {
      const bytes = await response.arrayBuffer();
      const signature = new Uint8Array(bytes);
      // The API returns application/octet-stream. Detect safe raster formats for previews.
      const mime = signature[0] === 0x89 && signature[1] === 0x50 ? 'image/png'
        : signature[0] === 0xff && signature[1] === 0xd8 ? 'image/jpeg'
        : signature[0] === 0x47 && signature[1] === 0x49 ? 'image/gif'
        : signature[0] === 0x52 && signature[8] === 0x57 ? 'image/webp' : 'application/octet-stream';
      data = new Blob([bytes], { type: mime });
      record.response = `Файл: ${data.size} байт · ${mime}`;
    } else {
      const raw = await response.text();
      try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
      record.response = data && typeof data === 'object' && 'image' in data ? { ...data, image: '[данные изображения]' } : data;
    }
    if (!response.ok) throw new Error(typeof data === 'object' && data ? (data.detail || data.title || `Ошибка ${response.status}`) : (data || `Ошибка ${response.status}`));
    const key = endpointKey(method, path);
    if (key) usedEndpoints.add(key);
    return data;
  } catch (error) {
    if (!record.status) record.response = error.message;
    throw new Error(record.status ? error.message : 'Сервер недоступен или не ответил вовремя. Проверьте запуск приложения и повторите запрос.');
  } finally {
    record.duration = Math.round(performance.now() - started);
    history.unshift(record);
    history.splice(100);
    window.dispatchEvent(new CustomEvent('api-request', { detail: record }));
  }
}
const id = encodeURIComponent;
export const api = {
  products: () => request('/products'), product: value => request(`/products/${id(value)}`),
  createProduct: body => request('/products', { method: 'POST', body }),
  decreaseStock: (value, decreaseBy) => request(`/products/${id(value)}/stock`, { method: 'PATCH', body: { decreaseBy } }),
  deleteProduct: value => request(`/products/${id(value)}`, { method: 'DELETE' }),
  clients: (limit, offset) => request('/clients' + (limit == null ? '' : `?limit=${limit}&offset=${offset}`)),
  searchClients: (name, surname) => request('/clients/search?' + new URLSearchParams({ name, surname })),
  client: value => request(`/clients/${id(value)}`), createClient: body => request('/clients', { method: 'POST', body }),
  clientAddress: (value, body) => request(`/clients/${id(value)}/address`, { method: 'PATCH', body }),
  deleteClient: value => request(`/clients/${id(value)}`, { method: 'DELETE' }),
  suppliers: () => request('/suppliers'), supplier: value => request(`/suppliers/${id(value)}`),
  createSupplier: body => request('/suppliers', { method: 'POST', body }),
  supplierAddress: (value, body) => request(`/suppliers/${id(value)}/address`, { method: 'PATCH', body }),
  deleteSupplier: value => request(`/suppliers/${id(value)}`, { method: 'DELETE' }),
  createImage: body => request('/images', { method: 'POST', body }),
  image: value => request(`/images/${id(value)}`, { binary: true }),
  productImage: value => request(`/images/product/${id(value)}`, { binary: true }),
  updateImage: (value, image) => request(`/images/${id(value)}`, { method: 'PUT', body: { image } }),
  deleteImage: value => request(`/images/${id(value)}`, { method: 'DELETE' }),
};
