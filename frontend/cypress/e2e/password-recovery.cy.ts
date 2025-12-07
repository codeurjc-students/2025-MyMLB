/// <reference types="cypress" />

describe('Password Recovery E2E Tests', () => {
	beforeEach(() => {
		cy.visit('/recovery');
	});

	const setPhase = (phase: 'email' | 'code' | 'password') => {
		cy.get('app-root').then(($root) => {
			cy.window().then((win) => {
				const rootCmp = (win as any).ng?.getComponent($root[0]);
				if (rootCmp && rootCmp.currentStep !== undefined) {
					rootCmp.currentStep = phase;
					(win as any).ng?.applyChanges?.();
				}
			});
		});
	};

	const fillEmailForm = (email: string) => {
		cy.get('app-email-phase input[formcontrolname=email]').type(email);
	};

	describe('Email Phase', () => {
		it('should successfully send the recovery email with a valid email', () => {
			cy.intercept('POST', '/api/v1/auth/forgot-password', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'Recovery email sent successfully' },
			}).as('succesfulEmail');

			cy.contains('h2', 'Password Recovery').should('be.visible');
			fillEmailForm('test@gmail.com');
			cy.get('app-email-phase').contains('button', 'SEND').click();

			cy.wait('@succesfulEmail');
			cy.contains('h2', 'Enter Verification Code').should('be.visible');
		});
	});

	describe('Code Phase', () => {
		beforeEach(() => {
			setPhase('code');
		});

		const fillCodeForm = (code: string) => {
			cy.get('app-code-phase input.code-input')
				.should('have.length', 4)
				.each(($input, index) => {
					cy.wrap($input).clear().type(code[index]);
				});
		};

		it('should show the code form and send the valid code', () => {
			cy.contains('h2', 'Enter Verification Code', { timeout: 2000 }).should(($el) => {
				expect($el[0].getBoundingClientRect().width).to.be.greaterThan(0);
			});

			fillCodeForm('1234');
			cy.get('app-code-phase').contains('button', 'Confirm').click();
			cy.contains('h2', 'Set New Password').should('be.visible');
		});
	});

	describe('New Password Phase', () => {
		beforeEach(() => {
			setPhase('password');
		});

		const fillPasswordForm = (password: string) => {
			cy.get('app-password-phase input[formcontrolname=newPassword]').type(password);
		};

		it('should send the password, reset it and navigate to the login form', () => {
			cy.intercept('POST', '/api/v1/auth/reset-password', {
				statusCode: 200,
				body: { status: 'SUCCESS', message: 'Password restored' },
			}).as('successfulReset');

			cy.contains('h2', 'Set New Password', { timeout: 2000 }).should(($el) => {
				expect($el[0].getBoundingClientRect().width).to.be.greaterThan(0);
			});

			fillPasswordForm('newPassword');
			cy.get('app-password-phase').contains('button', 'CONFIRM').click();

			cy.wait('@successfulReset');

			cy.get('app-password-phase').contains('p', 'Password restored').should('be.visible');
			cy.get('app-password-phase').contains('button', 'Confirm').click();

			cy.url().should('include', 'login');
			cy.get('app-login').contains('h2', 'Sign In').should('be.visible');
		});
	});
});