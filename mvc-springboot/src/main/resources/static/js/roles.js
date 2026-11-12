(() => {
  'use strict';
  const imageUrl = new URL('../img/hospital-vitarte.jpg', document.currentScript.src).href;
  const panels = {
    'Médico Solicitante': {
      title: 'Atención y solicitudes clínicas',
      description: 'Organiza las solicitudes transfusionales y consulta la información del paciente.',
      stock: false,
      actions: [['pacientes','Pacientes','Consulta los receptores y su historia clínica.'],['solicitudes','Solicitudes clínicas','Registra pedidos y revisa su estado.']]
    },
    'Tecnólogo Médico': {
      title: 'Control técnico del banco de sangre',
      description: 'Revisa el stock y continúa con el registro, tamizaje y etiquetado de las unidades.',
      stock: true,
      actions: [['donantes','Donantes y tamizaje','Registra donaciones y resultados serológicos.'],['inventario','Inventario','Revisa disponibilidad y vencimientos.'],['etiquetado','Etiquetado','Identifica las unidades con su código ISBT 128.'],['hemovigilancia','Hemovigilancia','Registra y consulta eventos transfusionales.']]
    },
    'Jefe de Banco de Sangre': {
      title: 'Coordinación y abastecimiento',
      description: 'Supervisa la disponibilidad, coordina las entregas y consulta los reportes del servicio.',
      stock: true,
      actions: [['despacho','Despacho','Verifica la entrega de las unidades.'],['interhospitalario','Red de hospitales','Coordina solicitudes de intercambio.'],['reportes','Reportes del servicio','Consulta y descarga los consolidados.']]
    },
    'Administrador': {
      title: 'Administración y supervisión',
      description: 'Gestiona usuarios y roles, revisa la actividad y accede a los módulos del sistema.',
      stock: false,
      actions: [['administracion','Usuarios y roles','Gestiona las cuentas y sus roles asignados.'],['auditoria','Actividad del sistema','Revisa las acciones registradas.'],['reportes','Reportes','Consulta los consolidados del servicio.']]
    }
  };
  document.addEventListener('DOMContentLoaded', () => {
    const dashboard = document.querySelector('#abo-grid')?.closest('section');
    const intro = dashboard?.querySelector('.workspace-intro');
    if (!intro) return;
    const actions = document.createElement('div');
    actions.className = 'role-actions';
    intro.after(actions);
    const picture = document.createElement('img');
    picture.src = imageUrl;
    picture.alt = 'Fachada del Hospital de Lima Este–Vitarte';
    picture.title = 'Stephaniedayana / Wikimedia Commons · CC BY-SA 4.0';
    picture.className = 'role-picture';
    picture.width = 600;
    picture.height = 400;
    const copy = document.createElement('div');
    copy.className = 'role-intro-copy';
    intro.replaceChildren(copy,picture);
    const loginImage = picture.cloneNode();
    loginImage.className = 'login-team-image';
    document.querySelector('.login-left')?.append(loginImage);
    function renderPanel() {
      const role = state.currentRole;
      const panel = panels[role];
      if (!panel) return;
      const allowed = Array.isArray(state.vistasPermitidas) ? state.vistasPermitidas : vistasPermitidas(role);
      const roleLabel = document.createElement('span');
      roleLabel.className = 'role-label';
      roleLabel.textContent = role;
      const title = document.createElement('h2'); title.textContent = panel.title;
      const description = document.createElement('p'); description.textContent = panel.description;
      copy.replaceChildren(roleLabel,title,description);
      actions.replaceChildren();
      panel.actions.filter(([view]) => allowed.includes(view)).forEach(([view,title,description]) => {
        const link = document.createElement('a');
        link.className = 'card role-action';
        link.href = window.PERSISTENCIA === 'servidor' ? '/' + view : '#' + view;
        if (window.PERSISTENCIA !== 'servidor') link.addEventListener('click', event => {
          event.preventDefault(); showView(view);
        });
        const heading = document.createElement('h3'); heading.textContent = title;
        const text = document.createElement('p'); text.textContent = description;
        const open = document.createElement('span'); open.className = 'role-open'; open.textContent = 'Abrir módulo →';
        link.append(heading,text,open); actions.append(link);
      });
      // El enfoque del panel no cambia los permisos que aplica el servidor.
      for (const child of dashboard.children) {
        if (child === intro || child === actions) continue;
        const consultas = child.id === 'dashboard-consultas' || child.id === 'dashboard-seguimiento'
          || child.classList.contains('dashboard-metrics');
        const stockBlock = child.classList.contains('stock-charts') || child.querySelector('#abo-grid');
        child.hidden = !(consultas || (panel.stock && stockBlock));
      }
    }
    renderPanel();
    const roleDisplay = document.querySelector('#user-role-display');
    if (roleDisplay) new MutationObserver(renderPanel).observe(roleDisplay,{childList:true,subtree:true});
  });
})();
