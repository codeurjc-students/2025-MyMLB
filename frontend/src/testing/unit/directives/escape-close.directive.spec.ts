import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { EscapeCloseDirective } from '../../../app/directives/escape-close.directive';
import { CommonModule } from '@angular/common';

@Component({
	standalone: true,
	imports: [CommonModule, EscapeCloseDirective],
	template: ` <div [appEscapeClose]="onClose"></div> `,
})
class TestHostComponent {
	public wasClosed = false;

	onClose = () => {
		this.wasClosed = true;
	};
}

describe('Escape Close Directive Tests', () => {
	let fixture: ComponentFixture<TestHostComponent>;
	let hostComponent: TestHostComponent;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [TestHostComponent, EscapeCloseDirective],
		});

		fixture = TestBed.createComponent(TestHostComponent);
		hostComponent = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should call closeFunc when Escape key is pressed', () => {
		const event = new KeyboardEvent('keydown', { key: 'Escape' });
		document.dispatchEvent(event);

		expect(hostComponent.wasClosed).toBeTrue();
	});

	it('should not call closeFunc when another key is pressed', () => {
		const event = new KeyboardEvent('keydown', { key: 'Enter' });
		document.dispatchEvent(event);

		expect(hostComponent.wasClosed).toBeFalse();
	});
});