/* Mejoras de presentación; no modifica datos, permisos ni persistencia. */
(() => {
  'use strict';
  const paths = {
    dashboard:'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z',
    inventario:'M3 7l9-4 9 4-9 4z M3 7v10l9 4 9-4V7 M12 11v10',
    donantes:'M12 3s-7 8-7 12a7 7 0 0 0 14 0c0-4-7-12-7-12z M9 15h6 M12 12v6',
    pacientes:'M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0 M4 21v-2a8 8 0 0 1 16 0v2',
    etiquetado:'M3 4h8l10 10-7 7L3 10z M7 7h.01',
    solicitudes:'M8 4H5v17h14V4h-3 M8 2h8v5H8z M8 12h8 M8 16h5',
    despacho:'M3 6h11v12H3z M14 10h4l3 4v4h-7 M6 18v3 M18 18v3 M6 10h4',
    interhospitalario:'M9 3h6v6H9z M3 15h6v6H3z M15 15h6v6h-6z M12 9v3 M6 15v-3h12v3',
    movil:'M7 2h10v20H7z M10 18h4',
    hemovigilancia:'M12 2l8 4v6c0 5-8 10-8 10S4 17 4 12V6z M7 12h3l2-4 2 8 2-4h2',
    reportes:'M5 3h10l4 4v14H5z M14 3v5h5 M8 17v-4 M12 17v-7 M16 17v-5',
    auditoria:'M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14 M15 15l6 6 M10 6v4l3 2',
    administracion:'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8 M12 2v3 M12 19v3 M2 12h3 M19 12h3 M5 5l2 2 M17 17l2 2 M5 19l2-2 M17 7l2-2',
    menu:'M4 6h16 M4 12h16 M4 18h16'
  };
  function icon(name) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg','svg');
    svg.setAttribute('viewBox','0 0 24 24');
    svg.setAttribute('class','nav-icon');
    svg.setAttribute('aria-hidden','true');
    const path = document.createElementNS(svg.namespaceURI,'path');
    path.setAttribute('d',paths[name] || paths.dashboard);
    svg.append(path);
    return svg;
  }
  const root = document.documentElement;
  try {
    if (localStorage.getItem('bancosangre-tema') === 'oscuro') root.dataset.tema = 'oscuro';
  } catch {}
  document.addEventListener('DOMContentLoaded', () => {
    const themeTopbar = document.querySelector('.topbar');
    const login = document.querySelector('.login-right');
    const themeButton = document.createElement('button');
    themeButton.type = 'button';
    themeButton.className = 'theme-button';
    function updateThemeButton() {
      const dark = root.dataset.tema === 'oscuro';
      themeButton.textContent = dark ? '☀' : '☾';
      themeButton.title = dark ? 'Activar modo claro' : 'Activar modo oscuro';
      themeButton.setAttribute('aria-label', themeButton.title);
    }
    themeButton.addEventListener('click', () => {
      root.dataset.tema = root.dataset.tema === 'oscuro' ? 'claro' : 'oscuro';
      try { localStorage.setItem('bancosangre-tema', root.dataset.tema); } catch {}
      updateThemeButton();
    });
    updateThemeButton();
    (themeTopbar || login)?.append(themeButton);
    if (themeTopbar && login) {
      const loginButton = themeButton.cloneNode(true);
      loginButton.addEventListener('click', () => themeButton.click());
      login.append(loginButton);
      new MutationObserver(() => {
        loginButton.textContent = themeButton.textContent;
        loginButton.title = themeButton.title;
        loginButton.setAttribute('aria-label', themeButton.title);
      }).observe(themeButton, {childList:true});
    }
    // Solo superficies decorativas: los formularios y las tablas permanecen estables.
    const allowTilt = matchMedia('(hover:hover) and (pointer:fine) and (prefers-reduced-motion:no-preference)');
    document.querySelectorAll('.workspace-intro').forEach(panel => {
      let frame = 0;
      function resetTilt() {
        cancelAnimationFrame(frame);
        panel.style.removeProperty('--tilt-x');
        panel.style.removeProperty('--tilt-y');
      }
      panel.addEventListener('pointermove', event => {
        if (!allowTilt.matches || event.pointerType !== 'mouse') return;
        cancelAnimationFrame(frame);
        frame = requestAnimationFrame(() => {
          const rect = panel.getBoundingClientRect();
          const x = Math.max(-.5,Math.min(.5,(event.clientX-rect.left)/rect.width-.5));
          const y = Math.max(-.5,Math.min(.5,(event.clientY-rect.top)/rect.height-.5));
          panel.style.setProperty('--tilt-x',(-y*4)+'deg');
          panel.style.setProperty('--tilt-y',(x*4)+'deg');
        });
      });
      panel.addEventListener('pointerleave',resetTilt);
      panel.addEventListener('pointercancel',resetTilt);
      allowTilt.addEventListener('change',resetTilt);
      window.addEventListener('blur',resetTilt);
    });
    document.querySelectorAll('.field').forEach((field,i) => {
      const input = field.querySelector('input,select,textarea');
      const label = field.querySelector('label');
      if (input && label && !label.htmlFor) {
        if (!input.id) input.id = 'campo-ui-' + i;
        label.htmlFor = input.id;
      }
    });
    const app = document.querySelector('.app');
    if (!app) return;
    const sidebar = app.querySelector('.sidebar');
    const content = app.querySelector('.content');
    const topbar = app.querySelector('.topbar');
    sidebar.id = 'navegacion-principal';
    sidebar.querySelector('nav').setAttribute('aria-label','Módulos del banco de sangre');
    content.id = 'contenido-principal'; content.tabIndex = -1;
    const skip = document.createElement('a');
    skip.href = '#contenido-principal'; skip.className = 'skip-link'; skip.textContent = 'Saltar al contenido';
    app.prepend(skip);
    let menu = topbar.querySelector('.menu-btn');
    if (!menu) {menu = document.createElement('button'); menu.className = 'menu-btn'; topbar.prepend(menu);}
    menu.removeAttribute('onclick'); menu.type = 'button'; menu.replaceChildren(icon('menu'));
    menu.setAttribute('aria-label','Abrir navegación'); menu.setAttribute('aria-controls',sidebar.id);
    const close = document.createElement('button');
    close.className = 'menu-close'; close.type = 'button'; close.textContent = 'Cerrar menú'; sidebar.prepend(close);
    const backdrop = document.createElement('button');
    backdrop.type = 'button'; backdrop.className = 'menu-backdrop'; backdrop.tabIndex = -1;
    backdrop.setAttribute('aria-label','Cerrar navegación'); app.append(backdrop);
    const mobile = window.matchMedia('(max-width:760px)');
    function setMenu(open,restore = false) {
      app.classList.toggle('navigation-open',open);
      menu.setAttribute('aria-expanded',String(open));
      sidebar.inert = mobile.matches && !open;
      if (open && mobile.matches) close.focus();
      if (!open && restore) menu.focus();
    }
    menu.addEventListener('click', () => setMenu(!app.classList.contains('navigation-open')));
    close.addEventListener('click', () => setMenu(false,true));
    backdrop.addEventListener('click', () => setMenu(false,true));
    mobile.addEventListener('change', () => setMenu(false));
    document.addEventListener('keydown', event => {
      if (!mobile.matches || !app.classList.contains('navigation-open')) return;
      if (event.key === 'Escape') {setMenu(false,true); event.preventDefault();}
      if (event.key === 'Tab') {
        const items = [...sidebar.querySelectorAll('a[href],button')].filter(el => el.getClientRects().length && !el.disabled);
        const first = items[0], last = items[items.length-1];
        if (event.shiftKey && document.activeElement === first) {last.focus(); event.preventDefault();}
        else if (!event.shiftKey && document.activeElement === last) {first.focus(); event.preventDefault();}
      }
    });
    setMenu(false);
    const items = sidebar.querySelectorAll('.nav-item');
    const syncCurrent = () => items.forEach(item => {
      if (item.classList.contains('active')) item.setAttribute('aria-current','page');
      else item.removeAttribute('aria-current');
    });
    items.forEach(item => {
      const key = item.dataset.view || new URL(item.href).pathname.split('/').pop();
      item.querySelector('.dot')?.remove(); item.prepend(icon(key));
      item.addEventListener('click', () => {if (mobile.matches) {setMenu(false); content.focus();}});
      new MutationObserver(syncCurrent).observe(item,{attributes:true,attributeFilter:['class']});
    });
    syncCurrent();
    const hospital = document.createElement('div');
    hospital.className = 'hospital-label';
    hospital.innerHTML = '<strong>Hospital Lima Este Vitarte</strong>Hemoterapia y Banco de Sangre';
    topbar.insertBefore(hospital, themeButton);
    document.querySelectorAll('.content table').forEach(table => {
      if (table.closest('.table-scroll')) return;
      const wrap = document.createElement('div');
      wrap.className = 'table-scroll'; wrap.tabIndex = 0; wrap.setAttribute('role','region');
      wrap.setAttribute('aria-label',table.closest('.card')?.querySelector('.card-title')?.textContent || 'Tabla de registros');
      table.before(wrap); wrap.append(table);
    });
    document.querySelector('#toast-wrap')?.setAttribute('aria-live','polite');
    const stockGrid = document.querySelector('#abo-grid');
    const metrics = document.querySelector('.dashboard-metrics');
    if (stockGrid && metrics) {
      const charts = document.createElement('div');
      charts.className = 'stock-charts';
      charts.innerHTML = `<section class="card stock-chart">
        <div class="card-title-row"><div><h2 class="card-title">Disponibilidad por grupo</h2><p class="card-desc">Unidades de glóbulos rojos en el panel actual.</p></div>
        <label class="chart-filter">Factor Rh <select class="input" id="chart-rh"><option value="all">Todos</option><option value="+">Positivo (+)</option><option value="-">Negativo (−)</option></select></label></div>
        <div class="chart-bars" aria-label="Unidades disponibles por grupo sanguíneo"></div>
        <p class="chart-caption">Las barras comparten escala. «Crítico» corresponde a la alerta registrada en el stock.</p>
      </section><section class="card stock-chart">
        <h2 class="card-title">Distribución por factor Rh</h2><p class="card-desc">Participación sobre el total de grupos del panel.</p>
        <div class="rh-donut" role="img"><div class="rh-donut-center"><strong></strong><span>unidades</span></div></div>
        <div class="chart-legend"></div><p class="chart-caption chart-source"></p>
      </section>`;
      metrics.after(charts);
      const filter = charts.querySelector('select');
      const orderLabel = document.createElement('label');
      orderLabel.className = 'chart-filter';
      orderLabel.textContent = 'Orden ';
      const order = document.createElement('select');
      order.className = 'input';
      order.id = 'chart-order';
      order.innerHTML = '<option value="group">Grupo sanguíneo</option><option value="descending">Mayor stock</option><option value="critical">Críticos primero</option>';
      orderLabel.append(order);
      charts.querySelector('.card-title-row').append(orderLabel);
      const summary = document.createElement('p');
      summary.className = 'chart-caption';
      summary.setAttribute('aria-live','polite');
      charts.querySelector('.chart-bars').after(summary);
      const format = new Intl.NumberFormat('es-PE');
      function drawStockCharts() {
        const groups = (typeof state !== 'undefined' && Array.isArray(state.aboGroups) ? state.aboGroups : [])
          .filter(row => Array.isArray(row) && Number.isFinite(Number(row[1])) && Number(row[1]) >= 0)
          .map(row => ({name:String(row[0]),units:Number(row[1]),critical:row[3] === true}));
        const rh = name => /(?:-|−|NEG)$/i.test(name.trim()) ? '-' : /(?:\+|POS)$/i.test(name.trim()) ? '+' : '?';
        const bars = charts.querySelector('.chart-bars');
        bars.replaceChildren();
        const max = Math.max(1,...groups.map(g => g.units));
        const visible = groups.filter(g => filter.value === 'all' || rh(g.name) === filter.value);
        if (order.value === 'descending') visible.sort((a,b) => b.units-a.units);
        if (order.value === 'critical') visible.sort((a,b) => Number(b.critical)-Number(a.critical));
        summary.textContent = visible.length + ' grupos · ' + format.format(visible.reduce((sum,g) => sum+g.units,0)) + ' unidades en la selección';
        visible.forEach(g => {
          const row = document.createElement('div'); row.className = 'chart-bar-row';
          const name = document.createElement('strong'); name.textContent = g.name;
          const track = document.createElement('div'); track.className = 'chart-bar-track'; track.setAttribute('aria-hidden','true');
          const fill = document.createElement('div'); fill.className = 'chart-bar-fill' + (g.critical ? ' critical' : '');
          fill.style.width = (g.units/max*100)+'%'; track.append(fill);
          const value = document.createElement('span'); value.className = 'chart-bar-value'; value.textContent = format.format(g.units);
          if (g.critical) {const tag = document.createElement('small'); tag.textContent = 'Crítico'; value.append(tag);}
          row.append(name,track,value); bars.append(row);
          row.title = g.name + ': ' + format.format(g.units) + ' unidades' + (g.critical ? ' · Stock crítico' : '');
        });
        if (!visible.length) bars.textContent = 'No hay datos de stock para este filtro.';
        const total = groups.reduce((sum,g) => sum+g.units,0);
        const positive = groups.filter(g => rh(g.name)==='+').reduce((sum,g) => sum+g.units,0);
        const negative = groups.filter(g => rh(g.name)==='-').reduce((sum,g) => sum+g.units,0);
        const unknown = total-positive-negative;
        const donut = charts.querySelector('.rh-donut');
        const a = total ? positive/total*100 : 0, b = total ? (positive+negative)/total*100 : 0;
        donut.style.background = total ? `conic-gradient(#177e83 0% ${a}%,#b52149 ${a}% ${b}%,#768596 ${b}% 100%)` : '#dfe6eb';
        donut.querySelector('strong').textContent = format.format(total);
        donut.setAttribute('aria-label',`Total: ${total} unidades. Rh positivo: ${positive}. Rh negativo: ${negative}. Sin clasificar: ${unknown}.`);
        const legend = charts.querySelector('.chart-legend'); legend.replaceChildren();
        [['Rh positivo',positive,'#177e83'],['Rh negativo',negative,'#b52149'],...(unknown ? [['Sin clasificar',unknown,'#768596']] : [])].forEach(([label,count,color]) => {
          const line = document.createElement('div');
          const dot = document.createElement('i'); dot.style.background = color; dot.setAttribute('aria-hidden','true');
          const text = document.createElement('span'); text.textContent = label;
          const value = document.createElement('strong'); value.textContent = `${format.format(count)} · ${total ? (count/total*100).toFixed(1) : '0'}%`;
          line.append(dot,text,value); legend.append(line);
        });
        charts.querySelector('.chart-source').textContent = total ? 'Total calculado del stock por grupo; puede diferir de los indicadores generales de ejemplo.' : 'Sin unidades disponibles en los datos cargados.';
      }
      filter.addEventListener('change',drawStockCharts);
      order.addEventListener('change',drawStockCharts);
      new MutationObserver(drawStockCharts).observe(stockGrid,{childList:true});
      drawStockCharts();
    }
  });
})();
