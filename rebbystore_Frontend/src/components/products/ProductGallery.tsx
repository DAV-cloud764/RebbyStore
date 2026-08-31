import { useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface ProductGalleryProps {
  images: string[];
  name: string;
}

export function ProductGallery({ images, name }: ProductGalleryProps) {
  const [activeIdx, setActiveIdx] = useState(0);

  function prev() { setActiveIdx((i) => (i === 0 ? images.length - 1 : i - 1)); }
  function next() { setActiveIdx((i) => (i === images.length - 1 ? 0 : i + 1)); }

  return (
    <div className="flex flex-col gap-3">
      {/* Main image */}
      <div className="relative overflow-hidden bg-rs-surface aspect-[3/4]">
        <img
          src={images[activeIdx]}
          alt={`${name} — view ${activeIdx + 1}`}
          className="w-full h-full object-cover"
        />
        {images.length > 1 && (
          <>
            <button
              onClick={prev}
              className="absolute left-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/80 backdrop-blur-sm flex items-center justify-center hover:bg-white transition-colors"
              aria-label="Previous image"
            >
              <ChevronLeft size={16} />
            </button>
            <button
              onClick={next}
              className="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 bg-white/80 backdrop-blur-sm flex items-center justify-center hover:bg-white transition-colors"
              aria-label="Next image"
            >
              <ChevronRight size={16} />
            </button>
          </>
        )}
        <div className="absolute bottom-3 right-3 bg-white/80 backdrop-blur-sm px-2 py-1 text-xs text-rs-muted">
          {activeIdx + 1} / {images.length}
        </div>
      </div>

      {/* Thumbnails */}
      {images.length > 1 && (
        <div className="flex gap-2 overflow-x-auto pb-1">
          {images.map((img, i) => (
            <button
              key={i}
              onClick={() => setActiveIdx(i)}
              aria-label={`View image ${i + 1}`}
              className={`shrink-0 w-16 h-20 overflow-hidden border-2 transition-colors ${
                i === activeIdx ? 'border-rs-ink' : 'border-transparent hover:border-rs-border'
              }`}
            >
              <img src={img} alt="" className="w-full h-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
