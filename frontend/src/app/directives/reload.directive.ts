import { Directive, OnChanges, SimpleChanges, TemplateRef, ViewContainerRef } from '@angular/core';

@Directive({
	selector: '[appReload]',
})
export class ReloadDirective implements OnChanges {
	constructor(private templateRef: TemplateRef<any>, private viewContainerRef: ViewContainerRef) {
		this.viewContainerRef.createEmbeddedView(this.templateRef);
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['appReload']) {
			this.viewContainerRef.clear();
			this.viewContainerRef.createEmbeddedView(this.templateRef);
		}
	}
}