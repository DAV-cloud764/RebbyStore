interface BrandMarkProps {
  className?: string;
}

export default function BrandMark({
  className = "",
}: BrandMarkProps) {
  return (
    <img
      src="public/images/brand/rebby-store-mark.png"
      alt="RebbyStore logo"
      className={`rounded-full object-cover ${className}`}
    />
  );
}