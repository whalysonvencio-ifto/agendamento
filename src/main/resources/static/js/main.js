// main.js - Lógicas de interação frontend Vanilla JS

document.addEventListener("DOMContentLoaded", function () {
    
    // 1. Client-Side Validation e Prevenção de Duplo Envio (Loading State)
    const forms = document.querySelectorAll('.needs-validation');

    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            // Se o form é inválido, pára e mostra validações visuais
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                form.classList.add('was-validated');
            } else {
                // Form é válido, inicia o "Loading State" para prevenir Double Submission
                const submitBtn = form.querySelector('button[type="submit"]');
                if (submitBtn) {
                    // Impede novos cliques imediatos
                    if(submitBtn.hasAttribute('data-submitting')) {
                        event.preventDefault();
                        return;
                    }
                    submitBtn.setAttribute('data-submitting', 'true');
                    
                    // Salva texto original e injeta spinner
                    const originalText = submitBtn.innerHTML;
                    submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Aguarde...`;
                    submitBtn.classList.add('disabled');
                    // Nota: Não usamos submitBtn.disabled = true pois isso impede que o valor (name=X) do botão vá no POST se o backend precisar, 
                    // a classe .disabled do BS e CSS pointer-events: none garantem o travamento visual e físico.
                    submitBtn.style.pointerEvents = 'none';
                }
            }
        }, false);
    });

    // 2. Fechamento automático de alertas de sucesso após 5 segundos
    const successAlerts = document.querySelectorAll('.alert-success');
    successAlerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

});
