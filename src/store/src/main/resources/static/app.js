import { api, history, endpointCatalog, usedEndpoints } from './api.js';

const paths = {
  overview: ['Обзор', 'overview'], products: ['Товары', 'box'], clients: ['Клиенты', 'users'],
  suppliers: ['Поставщики', 'truck'], images: ['Медиатека', 'image'], activity: ['Журнал API', 'activity'],
};
const icons = {
  overview: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  box: '<path d="m12 3 9 5v8l-9 5-9-5V8zM3 8l9 5 9-5M12 13v8M7.5 5.5l9 5"/>',
  users: '<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6M18 15a5 5 0 0 1 3 5"/>',
  truck: '<path d="M3 5h11v12H3zM14 9h4l3 4v4h-7M14 13h7"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/>',
  image: '<rect x="3" y="3" width="18" height="18" rx="3"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/>',
  activity: '<path d="M3 12h4l3-8 4 16 3-8h4"/>', plus: '<path d="M12 5v14M5 12h14"/>',
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
  refresh: '<path d="M20 7a9 9 0 1 0 1 8M20 3v5h-5"/>', arrow: '<path d="M4 12h16m-6-6 6 6-6 6"/>',
  close: '<path d="m6 6 12 12M6 18 18 6"/>', edit: '<path d="m15 4 5 5M4 20l5-1L21 7l-5-5L4 14z"/>',
  trash: '<path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7M14 10v7"/>',
  minus: '<path d="M5 12h14"/>', wallet: '<rect x="3" y="5" width="18" height="15" rx="3"/><path d="M16 11h5v5h-5zM3 8V5l14-3v3"/>',
  check: '<path d="m5 12 4 4L19 6"/>', leaf: '<path d="M20 3C6 2 2 9 5 16s17 5 15-13ZM4 21 16 9"/>',
};
const icon = name => `<svg viewBox="0 0 24 24" aria-hidden="true">${icons[name] || icons.box}</svg>`;
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const money = value => new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 2 }).format(value || 0);
const number = value => new Intl.NumberFormat('ru-RU').format(value);
const shortId = value => value ? '…' + value.slice(-8) : '—';
const date = value => value ? new Intl.DateTimeFormat('ru-RU').format(new Date(value)) : '—';
const main = document.querySelector('#main');
const dialog = document.querySelector('#dialog');
const state = { page: 'overview', products: [], clients: [], suppliers: [], clientRows: [], offset: 0, limit: 10, clientSearch: null, query: '', category: '', stock: '', renderVersion: 0 };
let modalVersion = 0;
const objectUrls = new Set();
const modalUrls = new Set();
function revoke(urls) { for (const url of urls) URL.revokeObjectURL(url); urls.clear(); }
function toast(message, error = false) {
  const el = document.createElement('div'); el.className = 'toast' + (error ? ' error' : ''); el.textContent = message;
  document.querySelector('#toasts').append(el); setTimeout(() => el.remove(), 5500);
}
const button = (text, action, cls = '', attrs = '', glyph = '') => `<button type="button" class="button ${cls}" data-action="${action}" ${attrs}>${glyph ? icon(glyph) : ''}${text}</button>`;
const rowButton = (label, action, glyph, attrs = '') => `<button type="button" class="icon-button" title="${label}" aria-label="${label}" data-action="${action}" ${attrs}>${icon(glyph)}</button>`;
const empty = (title, text = '', glyph = 'box') => `<div class="empty-state">${icon(glyph)}<h3>${title}</h3><p>${text}</p></div>`;
function heading(title, subtitle, actions = '') { return `<div class="page-heading"><div><h1>${title}</h1><p class="subtitle">${subtitle}</p></div><div class="heading-actions">${button('Обновить', 'refresh', '', '', 'refresh')}${actions}</div></div>`; }
function showModal(title, subtitle, body) {
  modalVersion++; revoke(modalUrls);
  document.querySelector('#dialog-content').innerHTML = `<div class="dialog-header"><div><h2 id="dialog-title">${esc(title)}</h2><p class="subtitle">${esc(subtitle)}</p></div>${rowButton('Закрыть', 'close', 'close')}</div><div class="dialog-body">${body}</div>`;
  if (!dialog.open) dialog.showModal();
}
function closeModal() { modalVersion++; dialog.close(); revoke(modalUrls); }
dialog.addEventListener('close', () => { modalVersion++; revoke(modalUrls); });
dialog.addEventListener('click', event => { if (event.target === dialog) { const r = dialog.getBoundingClientRect(); if (event.clientX < r.left || event.clientX > r.right || event.clientY < r.top || event.clientY > r.bottom) closeModal(); } });
const field = (label, name, type = 'text', value = '', extra = '', help = '') => `<label class="field ${extra.includes('data-full') ? 'full' : ''}">${label}<input name="${name}" type="${type}" value="${esc(value)}" required ${extra}>${help ? `<small>${help}</small>` : ''}</label>`;
const uuidField = (label, name, value = '', list = '') => field(label, name, 'text', value, `data-full class="uuid-input" pattern="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}" placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ${list ? `list="${list}"` : ''}`, 'UUID существующей записи');
function form(title, subtitle, fields, onSubmit, submitLabel = 'Сохранить', danger = false) {
  showModal(title, subtitle, `<form id="modal-form"><div class="form-grid">${fields}</div><div class="form-error" role="alert"></div><div class="dialog-footer">${button('Отмена', 'close')}<button class="button ${danger ? 'danger' : 'primary'}" type="submit">${submitLabel}</button></div></form>`);
  document.querySelector('#modal-form').addEventListener('submit', async event => {
    event.preventDefault(); const el = event.currentTarget; const submit = el.querySelector('[type=submit]');
    submit.disabled = true; el.querySelector('.form-error').textContent = '';
    try { await onSubmit(Object.fromEntries(new FormData(el)), el); }
    catch (error) { if (el.isConnected) el.querySelector('.form-error').textContent = error.message; else toast(error.message, true); }
    finally { submit.disabled = false; }
  });
}
function datalist(name, entries) { return `<datalist id="${name}">${entries.map(([value, label]) => `<option value="${esc(value)}">${esc(label)}</option>`).join('')}</datalist>`; }
function knownAddresses() {
  return datalist('addresses', [...new Map([...state.clients, ...state.suppliers].filter(x => x.addressId).map(x => [x.addressId, [x.addressId, x.name + ' ' + (x.surname || '')]])).values()]);
}
async function loadData() {
  const results = await Promise.allSettled([api.products(), api.clients(), api.suppliers()]);
  results.forEach((r, i) => { if (r.status === 'fulfilled') state[['products', 'clients', 'suppliers'][i]] = r.value; });
  const failure = results.find(r => r.status === 'rejected');
  if (failure) throw failure.reason;
}
async function render() {
  const version = ++state.renderVersion; revoke(objectUrls);
  state.page = Object.hasOwn(paths, location.hash.slice(1)) ? location.hash.slice(1) : 'overview';
  document.querySelector('#breadcrumb').textContent = paths[state.page][0];
  document.title = `${paths[state.page][0]} — Forma`;
  document.querySelector('#navigation').innerHTML = Object.entries(paths).map(([key, [label, glyph]]) => `<a href="#${key}" class="nav-item ${state.page === key ? 'active' : ''}" title="${label}" ${state.page === key ? 'aria-current="page"' : ''}>${icon(glyph)}<span class="nav-label">${label}</span></a>`).join('');
  if (state.page === 'activity') { renderActivity(); return; }
  main.innerHTML = '<div class="loading"><span class="spinner"></span>Загружаем данные магазина…</div>';
  try {
    await loadData();
    if (version !== state.renderVersion) return;
    if (state.page === 'clients') {
      state.clientRows = state.clientSearch ? await api.searchClients(...state.clientSearch) : await api.clients(state.limit, state.offset);
      if (version !== state.renderVersion) return;
    }
    ({ overview: renderOverview, products: renderProducts, clients: renderClients, suppliers: renderSuppliers, images: renderImages })[state.page]();
    hydrateImages(main, objectUrls);
  } catch (error) {
    if (version === state.renderVersion) main.innerHTML = heading(paths[state.page][0], 'Рабочее пространство магазина') + `<div class="error-state"><h2>Не удалось загрузить данные</h2><p>${esc(error.message)}</p>${button('Попробовать снова', 'refresh', 'primary')}</div>`;
  }
}
function stockBadge(stock) { return `<span class="badge ${stock === 0 ? 'empty' : stock < 10 ? 'warning' : ''}">${stock === 0 ? 'Нет в наличии' : stock < 10 ? 'Мало на складе' : 'В наличии'}</span>`; }
function productTable(products) {
  if (!products.length) return empty('Товаров пока нет', 'Добавьте первый товар или измените фильтры.');
  return `<div class="table-scroll"><table><thead><tr><th>Товар</th><th>Категория</th><th>Цена</th><th>Остаток</th><th>Статус</th><th><span class="sr-only">Действия</span></th></tr></thead><tbody>${products.map(p => `<tr><td><div class="product-cell"><div class="product-thumb" data-image="${esc(p.imageId)}">${icon('box')}</div><button class="row-link" data-action="product-detail" data-id="${esc(p.id)}"><strong>${esc(p.name)}</strong><small>${esc(shortId(p.id))}</small></button></div></td><td>${esc(p.category || 'Без категории')}</td><td class="money">${money(p.price)}</td><td>${number(p.availableStock)} <span class="muted">шт.</span></td><td>${stockBadge(p.availableStock)}</td><td><div class="row-actions">${rowButton('Открыть товар', 'product-detail', 'arrow', `data-id="${esc(p.id)}"`)}${rowButton('Списать остаток', 'stock', 'minus', `data-id="${esc(p.id)}" ${p.availableStock <= 0 ? 'disabled' : ''}`)}</div></td></tr>`).join('')}</tbody></table></div>`;
}
function renderOverview() {
  const totalStock = state.products.reduce((sum, p) => sum + p.availableStock, 0);
  const totalValue = state.products.reduce((sum, p) => sum + p.availableStock * p.price, 0);
  const available = state.products.filter(p => p.availableStock > 0).length;
  const stats = [[state.products.length, 'Товаров в каталоге', `${available} в наличии`, 'box'], [totalStock, 'Единиц на складе', 'Актуальный остаток', 'overview'], [state.clients.length, 'Клиентов', 'Ваша клиентская база', 'users'], [state.suppliers.length, 'Поставщиков', 'Партнёры магазина', 'truck']];
  const categories = Object.entries(state.products.reduce((acc, p) => { const key = p.category || 'Без категории'; acc[key] = (acc[key] || 0) + 1; return acc; }, {})).sort((a,b) => b[1] - a[1]);
  main.innerHTML = heading('Магазин под контролем', 'Все важные цифры и ежедневные задачи — в одном месте.', button('Добавить товар', 'create-product', 'primary', '', 'plus')) +
    `<section class="hero"><div class="hero-copy"><span class="eyebrow">ВАШ МАГАЗИН. ВАШ РИТМ.</span><h2>Меньше рутины.<br>Больше возможностей.</h2><p>Управляйте ассортиментом, остатками и партнёрами.</p>${button('Перейти к товарам', 'navigate', 'lime', 'data-page="products"', 'arrow')}</div><div class="hero-art" aria-hidden="true"><div class="art-orbit"></div><div class="art-orbit second"></div><div class="art-orbit third"></div><div class="art-tile">${icon('box')}</div><span class="art-star">✳</span></div></section>` +
    `<section class="stats" aria-label="Статистика магазина">${stats.map(([value, title, foot, glyph]) => `<article class="stat"><div class="stat-top">${title}<span class="stat-icon">${icon(glyph)}</span></div><strong class="stat-number">${number(value)}</strong><div class="stat-foot"><strong>${foot}</strong></div></article>`).join('')}</section>` +
    `<section class="panel"><div class="panel-heading"><div><h2>Ваш ассортимент <span class="count">${state.products.length}</span></h2><p class="subtitle">Товары, которые делают ваш магазин особенным</p></div><button class="text-button" data-action="navigate" data-page="products">Все товары ${icon('arrow')}</button></div>${productTable(state.products.slice(0,5))}<div class="table-footer"><span>Показано ${Math.min(state.products.length,5)} из ${state.products.length} товаров</span><span>Данные из вашего магазина</span></div></section>` +
    `<div class="bottom-grid"><section class="panel"><div class="panel-heading"><h2>Ассортимент по категориям</h2><span class="count">${categories.length}</span></div>${categories.length ? categories.map(([name,count]) => `<div class="category-row"><div class="category-icon">${icon('box')}</div><div class="category-body"><div class="category-label"><span>${esc(name)}</span><span>${count} · ${Math.round(count/state.products.length*100)}%</span></div><div class="bar-track"><div class="bar-fill" style="width:${count/state.products.length*100}%"></div></div></div></div>`).join('') : empty('Категорий пока нет')}</section><section class="panel note-panel"><span class="note-icon">${icon('wallet')}</span><h2>${money(totalValue)}</h2><p>Стоимость всех товаров на складе по текущим ценам. Остатки обновляются после каждого списания.</p><button class="text-button" data-action="navigate" data-page="products">Управлять остатками ${icon('arrow')}</button></section></div>`;
}
function filteredProducts() { return state.products.filter(p => (!state.query || (p.name + ' ' + p.category).toLocaleLowerCase('ru').includes(state.query.toLocaleLowerCase('ru'))) && (!state.category || p.category === state.category) && (!state.stock || (state.stock === 'available' ? p.availableStock > 0 : state.stock === 'low' ? p.availableStock > 0 && p.availableStock < 10 : p.availableStock === 0))); }
function renderProducts() {
  const categories = [...new Set(state.products.map(p => p.category).filter(Boolean))];
  main.innerHTML = heading('Товары', 'Ассортимент, цены и остатки вашего магазина.', button('Добавить товар', 'create-product', 'primary', '', 'plus')) + `<section class="panel"><div class="panel-heading"><h2>Каталог товаров <span class="count">${state.products.length}</span></h2>${button('Найти по ID', 'lookup-product', '', '', 'search')}</div><div class="toolbar"><label class="search-wrap">${icon('search')}<input id="product-search" aria-label="Поиск товаров" placeholder="Поиск по названию или категории" value="${esc(state.query)}"></label><select class="filter-select" id="category-filter" aria-label="Категория"><option value="">Все категории</option>${categories.map(c => `<option ${state.category === c ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select><select class="filter-select" id="stock-filter" aria-label="Наличие"><option value="">Все остатки</option><option value="available">В наличии</option><option value="low">Мало на складе</option><option value="empty">Нет в наличии</option></select></div><div id="product-results">${productTable(filteredProducts())}</div><div class="table-footer"><span id="result-count">Найдено: ${filteredProducts().length}</span><span>Списание доступно в карточке товара</span></div></section>`;
  document.querySelector('#stock-filter').value = state.stock;
  const update = () => { revoke(objectUrls); document.querySelector('#product-results').innerHTML = productTable(filteredProducts()); document.querySelector('#result-count').textContent = 'Найдено: ' + filteredProducts().length; hydrateImages(main, objectUrls); };
  document.querySelector('#product-search').addEventListener('input', e => { state.query = e.target.value; update(); });
  document.querySelector('#category-filter').addEventListener('change', e => { state.category = e.target.value; update(); });
  document.querySelector('#stock-filter').addEventListener('change', e => { state.stock = e.target.value; update(); });
}
function renderClients() {
  main.innerHTML = heading('Клиенты', 'Люди, которые выбирают ваш магазин.', button('Добавить клиента', 'create-client', 'primary', '', 'plus')) + `<section class="panel"><div class="panel-heading"><h2>Клиентская база <span class="count">${state.clients.length}</span></h2>${button('Найти по ID', 'lookup-client', '', '', 'search')}</div><div class="toolbar"><form class="client-search" id="client-search">${field('Имя', 'name', 'text', state.clientSearch?.[0] || '', 'maxlength="100" placeholder="Например, Ivan"')}${field('Фамилия', 'surname', 'text', state.clientSearch?.[1] || '', 'maxlength="100" placeholder="Например, Petrov"')}<button class="button primary" type="submit">${icon('search')}Найти</button>${button('Сбросить', 'reset-clients')}</form></div>${state.clientSearch ? '<div class="notice" style="margin:0 23px 16px">Точный поиск по имени и фамилии на сервере.</div>' : ''}${state.clientRows.length ? `<div class="table-scroll"><table><thead><tr><th>Клиент</th><th>Дата рождения</th><th>Регистрация</th><th>ID адреса</th><th>Действия</th></tr></thead><tbody>${state.clientRows.map(c => `<tr><td><div class="product-cell"><span class="avatar">${esc(c.name.charAt(0)+c.surname.charAt(0))}</span><button class="row-link" data-action="client-detail" data-id="${esc(c.id)}"><strong>${esc(c.name)} ${esc(c.surname)}</strong><small>${esc(shortId(c.id))}</small></button></div></td><td>${date(c.birthday)}</td><td>${date(c.registrationDate)}</td><td title="${esc(c.addressId)}">${esc(shortId(c.addressId))}</td><td><div class="row-actions">${rowButton('Открыть клиента', 'client-detail', 'arrow', `data-id="${esc(c.id)}"`)}${rowButton('Изменить адрес клиента', 'client-address', 'edit', `data-id="${esc(c.id)}"`)}</div></td></tr>`).join('')}</tbody></table></div>` : empty('Клиенты не найдены', 'Измените запрос или добавьте нового клиента.', 'users')}<div class="table-footer"><span>${state.clientSearch ? 'Найдено: ' + state.clientRows.length : 'Записи ' + (state.clientRows.length ? state.offset + 1 : 0) + '–' + (state.offset + state.clientRows.length)}</span>${!state.clientSearch ? `<div class="pagination"><select id="client-limit" class="filter-select" aria-label="Клиентов на странице">${[5,10,20,50].map(n => `<option value="${n}" ${n === state.limit ? 'selected' : ''}>${n} на странице</option>`).join('')}</select>${button('←', 'prev-clients', '', state.offset === 0 ? 'disabled aria-label="Предыдущая страница"' : 'aria-label="Предыдущая страница"')}${button('→', 'next-clients', '', state.offset + state.clientRows.length >= state.clients.length ? 'disabled aria-label="Следующая страница"' : 'aria-label="Следующая страница"')}</div>` : ''}</div></section>`;
  document.querySelector('#client-search').addEventListener('submit', e => { e.preventDefault(); const d = new FormData(e.currentTarget); state.clientSearch = [d.get('name').trim(), d.get('surname').trim()]; state.offset = 0; render(); });
  document.querySelector('#client-limit')?.addEventListener('change', e => { state.limit = Number(e.target.value); state.offset = 0; render(); });
}
function renderSuppliers() {
  main.innerHTML = heading('Поставщики', 'Надёжные партнёры и их контактные данные.', button('Добавить поставщика', 'create-supplier', 'primary', '', 'plus')) + `<section class="panel"><div class="panel-heading"><h2>Партнёры магазина <span class="count">${state.suppliers.length}</span></h2>${button('Найти по ID', 'lookup-supplier', '', '', 'search')}</div>${state.suppliers.length ? `<div class="table-scroll"><table><thead><tr><th>Поставщик</th><th>Телефон</th><th>Товаров</th><th>ID адреса</th><th>Действия</th></tr></thead><tbody>${state.suppliers.map(s => `<tr><td><div class="product-cell"><span class="product-thumb">${icon('truck')}</span><button class="row-link" data-action="supplier-detail" data-id="${esc(s.id)}"><strong>${esc(s.name)}</strong><small>${esc(shortId(s.id))}</small></button></div></td><td>${esc(s.phoneNumber)}</td><td>${state.products.filter(p => p.supplierId === s.id).length}</td><td title="${esc(s.addressId)}">${esc(shortId(s.addressId))}</td><td><div class="row-actions">${rowButton('Открыть поставщика', 'supplier-detail', 'arrow', `data-id="${esc(s.id)}"`)}${rowButton('Изменить адрес поставщика', 'supplier-address', 'edit', `data-id="${esc(s.id)}"`)}</div></td></tr>`).join('')}</tbody></table></div>` : empty('Поставщиков пока нет', 'Добавьте первого партнёра магазина.', 'truck')}</section>`;
}
function renderImages() {
  const images = [...new Map(state.products.filter(p => p.imageId).map(p => [p.imageId, p])).values()];
  main.innerHTML = heading('Медиатека', 'Изображения, которые помогают вашим товарам выделиться.', button('Загрузить изображение', 'create-image', 'primary', '', 'plus')) + `<div class="notice">Здесь показаны изображения из текущего каталога. Другие изображения можно открыть по ID. Загрузка нового изображения привязывает его к выбранному товару.</div><section class="panel" style="margin-bottom:22px"><div class="panel-heading"><h2>Быстрый доступ</h2><div class="heading-actions">${button('По ID изображения', 'lookup-image', '', '', 'search')}${button('По ID товара', 'lookup-product-image', '', '', 'box')}</div></div></section><div class="media-grid">${images.map(p => `<article class="panel media-card"><div class="media-preview" data-image="${esc(p.imageId)}">${icon('image')}</div><h3>${esc(p.name)}</h3><div class="mono">${esc(p.imageId)}</div><div class="row-actions">${button('Открыть', 'image-detail', '', `data-id="${esc(p.imageId)}"`)}${button('Заменить', 'update-image', '', `data-id="${esc(p.imageId)}"`)}${rowButton('Удалить изображение', 'delete-image', 'trash', `data-id="${esc(p.imageId)}"`)}</div></article>`).join('')}</div>${!images.length ? empty('Изображений пока нет', 'Загрузите изображение для существующего товара.', 'image') : ''}`;
}
function renderActivity() {
  main.innerHTML = heading('Журнал API', 'Запросы текущей сессии и покрытие всех возможностей API.', button('Очистить журнал', 'clear-history')) + `<div class="notice">Успешно использовано ${usedEndpoints.size} из ${endpointCatalog.length} endpoints. Запросы выполняются через обычные действия в интерфейсе. Журнал хранит последние 100 запросов и очищается при перезагрузке страницы.</div><div class="activity-layout"><section class="panel"><div class="panel-heading"><h2>Последние запросы</h2><span class="count">${history.length}</span></div><div class="log-list">${history.length ? history.map(r => `<details class="log-row"><summary><div class="log-top"><span class="method ${r.method}">${r.method}</span><span class="mono">${esc('/api/v1'+r.path)}</span><span class="log-status ${r.status >= 400 || !r.status ? 'bad' : ''}">${r.status || 'Сеть'}</span></div><div class="log-meta">${r.at.toLocaleTimeString('ru-RU')} · ${r.duration} мс · раскрыть ответ</div></summary><pre>${esc(typeof r.response === 'string' ? r.response : JSON.stringify(r.response,null,2))}</pre></details>`).join('') : empty('Запросов пока нет', 'Откройте любой раздел магазина.', 'activity')}</div></section><section class="panel"><div class="panel-heading"><h2>Возможности API</h2><span class="count">${usedEndpoints.size}/${endpointCatalog.length}</span></div><div class="endpoint-list">${endpointCatalog.map(([method,path,title]) => `<div class="endpoint-row" title="${title}"><span class="method ${method}">${method}</span><span class="mono">${path}</span><span class="endpoint-check ${usedEndpoints.has(method+' '+path) ? 'used' : ''}" aria-label="${usedEndpoints.has(method+' '+path) ? 'Использован' : 'Ещё не использован'}">${usedEndpoints.has(method+' '+path) ? '✓' : '○'}</span></div>`).join('')}</div></section></div>`;
}
async function hydrateImages(root, urls) {
  const cache = new Map();
  await Promise.allSettled([...root.querySelectorAll('[data-image]')].map(async el => {
    const id = el.dataset.image; if (!id) return;
    try {
      if (!cache.has(id)) cache.set(id, api.image(id));
      const blob = await cache.get(id); if (!el.isConnected) return;
      const url = URL.createObjectURL(blob); urls.add(url);
      const image = new Image(); image.alt = 'Изображение товара';
      image.onload = () => { if (image.naturalWidth > 1 && el.isConnected) el.replaceChildren(image); };
      image.onerror = () => { el.title = 'Не удалось показать изображение'; };
      image.src = url;
    } catch { if (el.isConnected) el.title = 'Изображение недоступно'; }
  }));
}
function details(items) { return `<dl class="detail-list">${items.map(([label,value]) => `<dt>${label}</dt><dd>${esc(value ?? '—')}</dd>`).join('')}</dl>`; }
async function productDetail(id) {
  const p = await api.product(id);
  showModal(p.name, p.category || 'Карточка товара', `<div class="detail-image" id="product-preview">${icon('box')}</div>${details([['ID товара', p.id],['Цена',money(p.price)],['На складе',`${p.availableStock} шт.`],['Последняя закупка',date(p.lastUpdateDate)],['Поставщик',state.suppliers.find(s => s.id === p.supplierId)?.name || p.supplierId],['ID изображения',p.imageId]])}<div class="detail-actions">${button('Списать остаток','stock','primary',`data-id="${esc(p.id)}" ${p.availableStock <= 0 ? 'disabled' : ''}`,'minus')}${button('Загрузить фото','create-image','',`data-id="${esc(p.id)}"`,'image')}${button('Удалить','delete-product','',`data-id="${esc(p.id)}"`,'trash')}</div>`);
  const current = modalVersion;
  try { const blob = await api.productImage(id); if (current !== modalVersion) return; previewBlob(blob, document.querySelector('#product-preview')); } catch (error) { if (current === modalVersion) document.querySelector('#product-preview').innerHTML = `<span class="blank-image">${esc(error.message)}</span>`; }
}
async function personDetail(type,id) {
  const client = type === 'client'; const data = await api[type](id);
  showModal(data.name + (client ? ' ' + data.surname : ''), client ? 'Карточка клиента' : 'Карточка поставщика', details([['ID',data.id],...(client ? [['Дата рождения',date(data.birthday)],['Пол',data.gender === 'MALE' ? 'Мужской' : 'Женский'],['Регистрация',date(data.registrationDate)]] : [['Телефон',data.phoneNumber]]),['ID адреса',data.addressId]]) + `<div class="detail-actions">${button('Изменить адрес',type+'-address','primary',`data-id="${esc(id)}"`,'edit')}${button('Удалить','delete-'+type,'',`data-id="${esc(id)}"`,'trash')}</div>`);
}
function previewBlob(blob, el) {
  if (!blob.type.startsWith('image/')) { el.innerHTML = '<span class="blank-image">Файл не является поддерживаемым изображением (PNG, JPEG, GIF, WebP).</span>'; return; }
  const url = URL.createObjectURL(blob); modalUrls.add(url); const image = new Image(); image.alt = 'Предпросмотр изображения'; image.className = 'standalone-image';
  image.onerror = () => { if (el.isConnected) el.innerHTML = '<span class="blank-image">Не удалось декодировать изображение.</span>'; };
  image.onload = () => { if (image.naturalWidth === 1 && el.isConnected) el.innerHTML = '<span class="blank-image">В базе сохранена заглушка размером 1 × 1 пиксель.<br>Загрузите фотографию товара.</span>'; };
  el.replaceChildren(image); image.src = url;
}
async function imageDetail(id, byProduct = false) {
  const blob = await (byProduct ? api.productImage(id) : api.image(id));
  showModal(byProduct ? 'Изображение товара' : 'Изображение', id, `<div id="image-preview" class="detail-image" style="height:280px"></div><div class="detail-actions">${!byProduct ? button('Заменить','update-image','primary',`data-id="${esc(id)}"`,'edit') + button('Удалить','delete-image','',`data-id="${esc(id)}"`,'trash') : ''}</div>`);
  previewBlob(blob,document.querySelector('#image-preview'));
}
async function saved(message) { closeModal(); toast(message); await render(); }
function createProduct() {
  const supplierList = datalist('supplier-ids', state.suppliers.map(s => [s.id,s.name]));
  const imageList = datalist('image-ids', [...new Map(state.products.filter(p => p.imageId).map(p => [p.imageId,[p.imageId,p.name]])).values()]);
  form('Новый товар','Пополните ассортимент магазина.',field('Название','name','text','','data-full maxlength="100"') + field('Категория','category','text','','data-full maxlength="100"') + field('Цена, ₽','price','number','','min="0.01" max="9999999999.99" step="0.01"') + field('Количество, шт.','availableStock','number','','min="1" max="2147483647" step="1"') + uuidField('Поставщик','supplierId','','supplier-ids') + uuidField('Изображение','imageId','','image-ids') + supplierList + imageList + '<div class="notice full">Выберите существующее изображение. После создания товара можно загрузить новую фотографию в его карточке.</div>', async d => { await api.createProduct({...d,price:Number(d.price),availableStock:Number(d.availableStock)}); await saved('Товар добавлен'); }, 'Создать товар');
}
function createPerson(type) {
  const client = type === 'client';
  const today = new Date().toLocaleDateString('en-CA');
  form(client ? 'Новый клиент' : 'Новый поставщик','Заполните основные данные.',field(client ? 'Имя' : 'Название компании','name','text','','data-full maxlength="100"') + (client ? field('Фамилия','surname','text','','data-full maxlength="100"') + field('Дата рождения','birthday','date','','max="'+today+'"') + `<label class="field">Пол<select name="gender" required><option value="MALE">Мужской</option><option value="FEMALE">Женский</option></select></label>` + field('Дата регистрации','registrationDate','date',today,'data-full') : field('Телефон','phoneNumber','tel','','data-full maxlength="30"')) + uuidField('Адрес','addressId','','addresses') + knownAddresses() + '<div class="notice full">Выберите ID известного адреса или введите другой существующий UUID. Создание и чтение адресов отдельно в текущем API не предусмотрено.</div>',async d => { await api[client ? 'createClient' : 'createSupplier'](d); state.clientSearch = null; state.offset = 0; await saved(client ? 'Клиент добавлен' : 'Поставщик добавлен'); },'Создать');
}
function changeAddress(type,id) {
  const owner = state[type === 'client' ? 'clients' : 'suppliers'].find(x => x.id === id);
  form('Изменить адрес',owner?.name || id,field('Страна','country','text','','data-full maxlength="30"') + field('Город','city','text','','data-full maxlength="100"') + field('Улица, дом','street','text','','data-full maxlength="150"') + '<div class="notice full">API изменяет существующую запись адреса. Если её используют другие клиенты или поставщики, их адрес тоже изменится.</div>',async d => { await api[type+'Address'](id,d); await saved('Адрес обновлён'); });
}
async function stockForm(id) {
  const p = await api.product(id);
  form('Списать со склада',`${p.name} · доступно ${p.availableStock} шт.`,field('Количество для списания','decreaseBy','number',1,`data-full min="1" max="${p.availableStock}" step="1"`),async d => { await api.decreaseStock(id,Number(d.decreaseBy)); await saved('Остаток обновлён'); },'Списать');
}
async function fileBase64(file) {
  if (!(file instanceof File) || !file.size) throw new Error('Выберите изображение.');
  if (!['image/png','image/jpeg','image/gif','image/webp'].includes(file.type)) throw new Error('Поддерживаются PNG, JPEG, GIF и WebP.');
  if (file.size > 5 * 1024 * 1024) throw new Error('Размер изображения не должен превышать 5 МБ.');
  return new Promise((resolve,reject) => { const reader = new FileReader(); reader.onload = () => resolve(reader.result.split(',')[1]); reader.onerror = () => reject(new Error('Не удалось прочитать файл.')); reader.readAsDataURL(file); });
}
function imageForm(id = '', update = false) {
  form(update ? 'Заменить изображение' : 'Загрузить изображение', update ? 'Замена отразится у всех товаров с этим изображением.' : 'Новая фотография будет привязана к товару.', (update ? '' : uuidField('Товар','productId',id,'product-ids') + datalist('product-ids',state.products.map(p => [p.id,p.name]))) + '<label class="field full">Файл изображения<input type="file" name="file" accept="image/png,image/jpeg,image/gif,image/webp" required><small>PNG, JPEG, GIF или WebP · до 5 МБ</small></label>',async d => { const image = await fileBase64(d.file); if (update) await api.updateImage(id,image); else await api.createImage({productId:d.productId,image}); await saved(update ? 'Изображение заменено' : 'Изображение загружено'); },update ? 'Заменить' : 'Загрузить');
}
function deleteForm(type,id) {
  const labels = {product:'товар',client:'клиента',supplier:'поставщика',image:'изображение'};
  const detail = type === 'supplier' ? 'Удаление поставщика также удалит все его товары из-за каскадного удаления в базе.' : type === 'image' ? 'Если изображение используется товаром, база отклонит удаление. Сначала загрузите другое изображение для связанных товаров.' : 'Действие нельзя отменить.';
  form('Удалить '+labels[type]+'?',id,`<p class="subtitle full">${detail}</p>`,async () => { await api['delete'+type[0].toUpperCase()+type.slice(1)](id); await saved('Запись удалена'); },'Удалить',true);
}
function lookup(type) {
  const names = {product:'товара',client:'клиента',supplier:'поставщика',image:'изображения','product-image':'товара'};
  form('Найти по ID','Введите UUID '+names[type]+'.',uuidField('ID','id'),async d => {
    if(type === 'product') await productDetail(d.id);
    else if(type === 'client' || type === 'supplier') await personDetail(type,d.id);
    else await imageDetail(d.id,type === 'product-image');
  },'Открыть');
}
document.addEventListener('click',async event => {
  const target = event.target.closest('[data-action]'); if (!target || target.disabled) return;
  const action = target.dataset.action; const id = target.dataset.id;
  try {
    if (action === 'close') closeModal();
    else if (action === 'navigate') location.hash = target.dataset.page;
    else if (action === 'refresh') await render();
    else if (action === 'create-product') createProduct();
    else if (action === 'create-client') createPerson('client');
    else if (action === 'create-supplier') createPerson('supplier');
    else if (action === 'product-detail') { target.disabled = true; await productDetail(id); }
    else if (action === 'client-detail' || action === 'supplier-detail') { target.disabled = true; await personDetail(action.split('-')[0],id); }
    else if (action === 'stock') { target.disabled = true; await stockForm(id); }
    else if (action.endsWith('-address')) changeAddress(action.split('-')[0],id);
    else if (action.startsWith('delete-')) deleteForm(action.slice(7),id);
    else if (action === 'create-image') imageForm(id);
    else if (action === 'update-image') imageForm(id,true);
    else if (action === 'image-detail') { target.disabled = true; await imageDetail(id); }
    else if (action.startsWith('lookup-')) lookup(action.slice(7));
    else if (action === 'reset-clients') { state.clientSearch = null; state.offset = 0; await render(); }
    else if (action === 'prev-clients' || action === 'next-clients') { state.offset = Math.max(0,state.offset + (action === 'next-clients' ? state.limit : -state.limit)); await render(); }
    else if (action === 'clear-history') { history.length = 0; renderActivity(); }
  } catch (error) { toast(error.message,true); }
  finally { target.disabled = false; }
});
window.addEventListener('api-request',event => {
  const el = document.querySelector('#connection'); const online = event.detail.status > 0;
  el.className = 'connection ' + (online ? 'online' : 'offline'); el.innerHTML = `<span class="status-dot"></span>${online ? 'API подключён' : 'Нет соединения'}`;
  if (state.page === 'activity') renderActivity();
});
window.addEventListener('hashchange', () => { closeModal(); render(); });
document.querySelector('#today').textContent = new Intl.DateTimeFormat('ru-RU',{day:'numeric',month:'long',year:'numeric'}).format(new Date());
render();
