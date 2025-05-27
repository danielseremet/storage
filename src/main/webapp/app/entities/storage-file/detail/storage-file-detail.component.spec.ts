import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { StorageFileDetailComponent } from './storage-file-detail.component';

describe('StorageFile Management Detail Component', () => {
  let comp: StorageFileDetailComponent;
  let fixture: ComponentFixture<StorageFileDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StorageFileDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./storage-file-detail.component').then(m => m.StorageFileDetailComponent),
              resolve: { storageFile: () => of({ id: 11027 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(StorageFileDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StorageFileDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load storageFile on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', StorageFileDetailComponent);

      // THEN
      expect(instance.storageFile()).toEqual(expect.objectContaining({ id: 11027 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
